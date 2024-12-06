package com.mct.mediapicker.fragment;

import static com.mct.mediapicker.MediaPickerOption.PICK_MODE_MULTI;
import static com.mct.mediapicker.MediaPickerOption.PICK_TYPE_ALL;
import static com.mct.mediapicker.MediaPickerOption.PICK_TYPE_IMAGE;
import static com.mct.mediapicker.MediaPickerOption.PICK_TYPE_VIDEO;
import static com.mct.mediapicker.MediaPickerOption.PickType;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.mct.mediapicker.MediaPickerOption;
import com.mct.mediapicker.model.Album;
import com.mct.mediapicker.model.Media;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

class Presenter {

    ///////////////////////////////////////////////////////////////////////////
    // Options keeper
    ///////////////////////////////////////////////////////////////////////////

    static void storeOption(@NonNull MediaPickerOption option) {
        OptionHolder.options.put(option.getId(), option);
    }

    private static class OptionHolder {
        private static final Map<String, MediaPickerOption> options = new ArrayMap<>();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Presenter area
    ///////////////////////////////////////////////////////////////////////////

    private Disposable albumDisposable;

    private String optionId;
    private MediaPickerOption option;
    private List<Media> selectedMedia;
    private List<Album> albums;
    private boolean isCallSubmitListener;

    public Presenter() {
    }

    public boolean isEmptyOption() {
        return option == null;
    }

    public void attach(String optionId) {
        this.optionId = optionId;
        this.option = OptionHolder.options.get(optionId);
    }

    public void detach() {
        if (albumDisposable != null) {
            albumDisposable.dispose();
            albumDisposable = null;
        }
        OptionHolder.options.remove(optionId);
        optionId = null;
        option = null;
        selectedMedia = null;
        albums = null;
    }

    public MediaPickerOption getOption() {
        return option;
    }

    public boolean isMultipleSelect() {
        return getOption().getPickMode() == PICK_MODE_MULTI;
    }

    public int getMaxSelection() {
        return getOption().getMaxSelection();
    }

    public void submitSelectedMedia() {
        if (isCallSubmitListener) {
            return;
        }
        isCallSubmitListener = true;
        if (selectedMedia == null || selectedMedia.isEmpty()) {
            Optional.ofNullable(getOption()).map(MediaPickerOption::getListener1).ifPresent(l -> l.onPick(null));
            Optional.ofNullable(getOption()).map(MediaPickerOption::getListener2).ifPresent(l -> l.onPick(null));
        } else {
            if (isMultipleSelect()) {
                Optional.ofNullable(getOption()).map(MediaPickerOption::getListener2)
                        .ifPresent(listener -> listener.onPick(selectedMedia.stream()
                                .filter(Objects::nonNull)
                                .map(Media::getUri)
                                .collect(Collectors.toList())));
            } else {
                Optional.ofNullable(getOption()).map(MediaPickerOption::getListener1)
                        .ifPresent(listener -> listener.onPick(selectedMedia.stream()
                                .filter(Objects::nonNull)
                                .map(Media::getUri)
                                .findFirst()
                                .orElse(null)));
            }
        }
    }

    public int getSelectedMediaCount() {
        if (selectedMedia == null) {
            return 0;
        }
        return selectedMedia.size();
    }

    public boolean isSelectedMedia(Media media) {
        if (selectedMedia == null) {
            return false;
        }
        return selectedMedia.contains(media);
    }

    public void addSelectedMedia(Media media) {
        if (selectedMedia == null) {
            selectedMedia = new ArrayList<>();
        }
        if (media == null || selectedMedia.contains(media)) {
            return;
        }
        selectedMedia.add(media);
    }

    public void removeSelectedMedia(Media media) {
        if (selectedMedia == null) {
            return;
        }
        selectedMedia.remove(media);
    }

    private final List<Consumer<List<Album>>> albumCallbacks = new ArrayList<>();

    public void getAlbums(Context context, @NonNull Consumer<List<Album>> callback) {
        if (isEmptyOption()) {
            return;
        }
        if (albums == null || albums.isEmpty()) {
            albumCallbacks.add(callback);
            // check disposable
            if (albumDisposable != null) {
                return;
            }
            albumDisposable = Single.fromCallable(() -> loadMedia(context, option.getPickType()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnDispose(albumCallbacks::clear)
                    .subscribe((loadedAlbums, t) -> {
                        if (isEmptyOption()) {
                            return;
                        }
                        albums = loadedAlbums;
                        albumDisposable = null;
                        albumCallbacks.forEach(c -> c.accept(albums));
                        albumCallbacks.clear();
                    });
        } else {
            callback.accept(albums);
        }
    }

    @NonNull
    private static List<Album> loadMedia(Context context, @PickType int pickType) {
        Uri contentUri;
        switch (pickType) {
            // @formatter:off
            case PICK_TYPE_IMAGE:   contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;  break;
            case PICK_TYPE_VIDEO:   contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;   break;
            case PICK_TYPE_ALL:     contentUri = MediaStore.Files.getContentUri("external");    break;
            default: throw new IllegalArgumentException("Unknown pick type: " + pickType);
            // @formatter:on
        }

        String[] projections = {
                MediaStore.MediaColumns.BUCKET_ID,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DURATION,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT,
        };
        String selection = null;
        String[] selectionArgs = null;
        String orderBy = MediaStore.MediaColumns.DATE_MODIFIED + " DESC";

        if (pickType == PICK_TYPE_ALL) {
            selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " IN (?, ?)";
            selectionArgs = new String[]{
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
            };
        }

        Cursor cursor;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Bundle queryArgs = new Bundle();
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection);
            queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs);
            cursor = context.getContentResolver().query(contentUri, projections, queryArgs, null);
        } else {
            cursor = context.getContentResolver().query(contentUri, projections, selection, selectionArgs, orderBy);
        }
        try {
            ArrayMap<String, Album> albums = new ArrayMap<>();
            if (cursor != null && cursor.moveToFirst()) {
                int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_ID);
                int bucketNameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME);
                int mediaPathIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                int mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE);
                int dateModified = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED);
                int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE);
                int durationIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION);
                int widthIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH);
                int heightIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT);

                do {
                    String path = cursor.getString(mediaPathIndex);
                    String bucketId = cursor.getString(bucketIdIndex);
                    Album album = albums.get(bucketId);

                    if (album == null) {
                        albums.put(bucketId, album = new Album(bucketId, cursor.getString(bucketNameIndex)));
                    }

                    Media media = new Media();
                    media.setUri(Uri.fromFile(new File(path)));
                    media.setName(getFileName(path));
                    media.setMimeType(cursor.getString(mimeTypeIndex));
                    media.setDateModified(cursor.getInt(dateModified));
                    media.setSize(cursor.getInt(sizeIndex));
                    media.setDuration(cursor.getInt(durationIndex));
                    media.setWidth(cursor.getInt(widthIndex));
                    media.setHeight(cursor.getInt(heightIndex));

                    album.addMedia(media);
                } while (cursor.moveToNext());
            }
            return new ArrayList<>(albums.values());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @NonNull
    private static String getFileName(@NonNull String path) {
        int lastIndex = path.lastIndexOf('/');
        if (lastIndex != -1) {
            return path.substring(lastIndex + 1);
        }
        return path;
    }

}
