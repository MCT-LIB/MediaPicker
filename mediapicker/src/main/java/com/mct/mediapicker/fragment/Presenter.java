package com.mct.mediapicker.fragment;

import static com.mct.mediapicker.MediaPickerOption.PICK_MODE_MULTI;
import static com.mct.mediapicker.MediaPickerOption.PICK_TYPE_ALL;
import static com.mct.mediapicker.MediaPickerOption.PICK_TYPE_IMAGE;
import static com.mct.mediapicker.MediaPickerOption.PICK_TYPE_VIDEO;
import static com.mct.mediapicker.MediaPickerOption.PickType;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Size;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.collection.LruCache;
import androidx.core.util.Consumer;
import androidx.core.util.Supplier;

import com.mct.mediapicker.MediaPickerOption;
import com.mct.mediapicker.MediaUtils;
import com.mct.mediapicker.common.DispatchQueue;
import com.mct.mediapicker.model.Album;
import com.mct.mediapicker.model.Media;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

class Presenter {

    private boolean isCallSubmitListener;
    private Disposable albumDisposable;

    private final List<Album> albums = new ArrayList<>();
    private final List<Media> selectedMedia = new ArrayList<>();
    private Supplier<MediaPickerOption> optionSupplier;

    public Presenter() {
    }

    public boolean isEmptyOption() {
        return optionSupplier == null;
    }

    public void attach(Supplier<MediaPickerOption> optionSupplier) {
        this.optionSupplier = optionSupplier;
    }

    public void detach() {
        if (albumDisposable != null) {
            albumDisposable.dispose();
            albumDisposable = null;
        }
        albums.clear();
        selectedMedia.clear();
        optionSupplier = null;
        MediaLoader.cleanupQueues();
    }

    public MediaPickerOption getOption() {
        return optionSupplier.get();
    }

    public boolean isMultipleSelect() {
        return getOption().getPickMode() == PICK_MODE_MULTI;
    }

    public void submitSelectedMedia() {
        if (isCallSubmitListener) {
            return;
        }
        isCallSubmitListener = true;
        if (selectedMedia.isEmpty()) {
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
        return selectedMedia.size();
    }

    public boolean isSelectedMedia(Media media) {
        return selectedMedia.contains(media);
    }

    public void addSelectedMedia(Media media) {
        if (media == null || selectedMedia.contains(media)) {
            return;
        }
        selectedMedia.add(media);
    }

    public void removeSelectedMedia(Media media) {
        selectedMedia.remove(media);
    }

    public List<Media> getSelectedMedia() {
        return selectedMedia;
    }

    public void setSelectedMedia(List<Media> selectedMedia) {
        if (selectedMedia == null) {
            this.selectedMedia.clear();
        } else {
            this.selectedMedia.clear();
            this.selectedMedia.addAll(selectedMedia);
        }
    }

    private final List<Consumer<List<Album>>> albumCallbacks = new ArrayList<>();

    public void getAlbums(Context context, @NonNull Consumer<List<Album>> callback) {
        if (isEmptyOption()) {
            return;
        }
        if (albums.isEmpty()) {
            albumCallbacks.add(callback);
            // check disposable
            if (albumDisposable != null) {
                return;
            }
            albumDisposable = Single.fromCallable(() -> loadMedia(context, getOption().getPickType()))
                    .delay((long) (Math.random() * 100), TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnDispose(albumCallbacks::clear)
                    .subscribe((loadedAlbums, t) -> {
                        if (isEmptyOption()) {
                            return;
                        }
                        albums.clear();
                        albums.addAll(loadedAlbums);
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
                MediaStore.MediaColumns._ID,
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
            ArrayMap<Integer, Album> albums = new ArrayMap<>();
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
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
                    int bucketId = cursor.getInt(bucketIdIndex);
                    Album album = albums.get(bucketId);
                    if (album == null) {
                        album = new Album(bucketId, cursor.getString(bucketNameIndex));
                        albums.put(bucketId, album);
                    }
                    String path = cursor.getString(mediaPathIndex);
                    Media media = new Media();
                    media.setId(cursor.getInt(idIndex));
                    media.setBucketId(bucketId);
                    media.setPath(path);
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

    ///////////////////////////////////////////////////////////////////////////
    // Media loader
    ///////////////////////////////////////////////////////////////////////////

    @NonNull
    static MediaLoaderDelegate create(Context context, boolean fullScreen) {
        return new MediaLoader(context, fullScreen);
    }

    private static class MediaLoader implements MediaLoaderDelegate {

        private final Context context;
        private final boolean fullScreen;

        private Media media;
        private MediaLoaderListener mediaLoaderListener;

        private String key;
        private Bitmap bitmap;
        private DispatchQueue loadQueue;
        private Runnable cancelTask;
        private Runnable loadingTask;

        private final Runnable unloadRunnable = () -> loadBitmap(null);

        private MediaLoader(@NonNull Context context, boolean fullScreen) {
            this.context = context.getApplicationContext();
            this.fullScreen = fullScreen;
        }

        private DispatchQueue getQueue() {
            if (loadQueue != null) {
                return loadQueue;
            }
            if (allQueues.size() < MAX_QUEUES) {
                loadQueue = new DispatchQueue("gallery_load_" + allQueues.size());
                allQueues.add(loadQueue);
            } else {
                currentQueueIndex = (currentQueueIndex + 1) % allQueues.size();
                loadQueue = allQueues.get(currentQueueIndex);
            }
            return loadQueue;
        }

        private void loadBitmap(Media media) {
            if (media == null) {
                releaseCurrentBitmap();
                return;
            }

            final String newKey = media.getId() + "_fs_" + fullScreen;
            if (TextUtils.equals(newKey, key)) {
                return;
            }

            releaseCurrentBitmap();
            key = newKey;

            bitmap = getBitmap(key);
            if (bitmap != null) {
                onBitmapChanged();
                return;
            }

            // Cancel previous task
            Optional.ofNullable(cancelTask).ifPresent(Runnable::run);
            Optional.ofNullable(loadingTask).ifPresent(getQueue()::cancelRunnable);

            // Create listener
            Consumer<Bitmap> afterLoad = bitmap -> {
                this.cancelTask = null;
                this.loadingTask = null;
                MediaUtils.runOnUiThread(() -> afterLoad(newKey, bitmap));
            };

            // Create new task
            if (fullScreen) {
                CancellationSignal cancelTaskSignal = new CancellationSignal();
                cancelTask = createCancelTask(context, media, cancelTaskSignal);
                loadingTask = createFullScreenLoadingTask(context, media, cancelTaskSignal, afterLoad);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    CancellationSignal cancelTaskSignal = new CancellationSignal();
                    cancelTask = createCancelTask(context, media, cancelTaskSignal);
                    loadingTask = createLoadingTask(context, media, cancelTaskSignal, afterLoad);
                } else {
                    cancelTask = createCancelTask(context, media, null);
                    loadingTask = createLoadingTask(context, media, afterLoad);
                }
            }

            // Start task
            getQueue().postRunnable(loadingTask);
        }

        private void afterLoad(String key, Bitmap loadedBitmap) {
            if (loadedBitmap == null) return;

            putBitmap(key, loadedBitmap);
            if (!TextUtils.equals(key, this.key)) {
                releaseBitmap(key);
                return;
            }

            bitmap = loadedBitmap;
            onBitmapChanged();
        }

        private void releaseCurrentBitmap() {
            if (key != null) {
                releaseBitmap(key);
                key = null;
            }
            bitmap = null;
            onBitmapChanged();
        }

        private void onBitmapChanged() {
            if (mediaLoaderListener != null) {
                mediaLoaderListener.onThumbnailLoaded(bitmap);
            }
        }

        @Override
        public void onAttach(View view) {
            MediaUtils.removeOnUiThread(unloadRunnable);
            if (media != null) {
                loadBitmap(media);
            }
        }

        @Override
        public void onDetach(View view) {
            MediaUtils.runOnUiThreadDelayed(unloadRunnable, 250);
        }

        @Override
        public void loadThumbnail(Media media) {
            this.media = media;
            loadBitmap(media);
        }

        @Override
        public void setListener(MediaLoaderListener listener) {
            this.mediaLoaderListener = listener;
        }

        @NonNull
        private static Runnable createFullScreenLoadingTask(
                @NonNull Context context,
                @NonNull Media media,
                @Nullable CancellationSignal cancelTaskSignal,
                @NonNull Consumer<Bitmap> afterLoad) {
            return () -> {
                AtomicReference<Bitmap> bitmapRef = new AtomicReference<>();
                try {
                    if (media.isVideo()) {
                        throw new UnsupportedOperationException("Video thumb full screen not support!");
                    }

                    // check cancel
                    if (cancelTaskSignal != null) cancelTaskSignal.throwIfCanceled();

                    Bitmap result;
                    Bitmap bitmap = BitmapFactory.decodeFile(media.getPath());

                    // check cancel
                    if (cancelTaskSignal != null) cancelTaskSignal.throwIfCanceled();

                    if (bitmap != null) {
                        int screenWidth = MediaUtils.getScreenWidth(context);
                        int originalWidth = bitmap.getWidth();
                        int originalHeight = bitmap.getHeight();
                        int w = screenWidth * 3 / 4;
                        int h = (originalHeight * w) / originalWidth;
                        result = Bitmap.createScaledBitmap(bitmap, w, h, true);
                        bitmap.recycle();
                    } else {
                        result = null;
                    }
                    bitmapRef.set(result);
                } catch (Exception e) {
                    bitmapRef.set(null);
                } finally {
                    afterLoad.accept(bitmapRef.get());
                }
            };
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        @NonNull
        private static Runnable createLoadingTask(
                @NonNull Context context,
                @NonNull Media media,
                @Nullable CancellationSignal cancelTaskSignal,
                @NonNull Consumer<Bitmap> afterLoad) {
            return () -> {
                AtomicReference<Bitmap> bitmapRef = new AtomicReference<>();
                try {
                    Bitmap bitmap;
                    ContentResolver cr = context.getContentResolver();
                    int size = Math.min(MediaUtils.getScreenWidth(context) / 3, 512);
                    if (media.isVideo()) {
                        Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, media.getId());
                        bitmap = cr.loadThumbnail(uri, new Size(size, size), cancelTaskSignal);
                    } else {
                        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, media.getId());
                        bitmap = cr.loadThumbnail(uri, new Size(size, size), cancelTaskSignal);
                    }
                    bitmapRef.set(bitmap);
                } catch (Exception e) {
                    bitmapRef.set(null);
                } finally {
                    afterLoad.accept(bitmapRef.get());
                }
            };
        }

        @SuppressWarnings("deprecation")
        @NonNull
        private static Runnable createLoadingTask(
                @NonNull Context context,
                @NonNull Media media,
                @NonNull Consumer<Bitmap> afterLoad) {
            return () -> {
                AtomicReference<Bitmap> bitmapRef = new AtomicReference<>();
                try {
                    Bitmap bitmap;
                    ContentResolver cr = context.getContentResolver();
                    if (media.isVideo()) {
                        int kind = MediaStore.Video.Thumbnails.MINI_KIND;
                        bitmap = MediaStore.Video.Thumbnails.getThumbnail(cr, media.getId(), media.getBucketId(), kind, null);
                    } else {
                        int kind = MediaStore.Images.Thumbnails.MINI_KIND;
                        bitmap = MediaStore.Images.Thumbnails.getThumbnail(cr, media.getId(), media.getBucketId(), kind, null);
                    }
                    bitmapRef.set(bitmap);
                } catch (Exception e) {
                    bitmapRef.set(null);
                } finally {
                    afterLoad.accept(bitmapRef.get());
                }
            };
        }

        @NonNull
        private static Runnable createCancelTask(
                @NonNull Context context,
                @NonNull Media media,
                @Nullable CancellationSignal cancelTaskSignal) {
            return () -> {
                if (cancelTaskSignal != null) {
                    cancelTaskSignal.cancel();
                } else {
                    ContentResolver cr = context.getContentResolver();
                    if (media.isVideo()) {
                        MediaStore.Video.Thumbnails.cancelThumbnailRequest(cr, media.getId(), media.getBucketId());
                    } else {
                        MediaStore.Images.Thumbnails.cancelThumbnailRequest(cr, media.getId(), media.getBucketId());
                    }
                }
            };
        }

        /* --- Queue and bitmap cache methods ---*/

        private static final int MAX_QUEUES = 4;
        private static int currentQueueIndex = 0;
        private static final List<DispatchQueue> allQueues = new ArrayList<>();
        private static final HashMap<String, Integer> bitmapsUseCounts = new HashMap<>();
        private static final LruCache<String, Bitmap> bitmapsCache = new LruCache<String, Bitmap>(45) {
            @Override
            protected void entryRemoved(boolean evicted, @NonNull String key, @NonNull Bitmap oldValue, Bitmap newValue) {
                if (oldValue.isRecycled() || bitmapsUseCounts.containsKey(key)) {
                    return;
                }
                oldValue.recycle();
            }
        };

        public static void cleanupQueues() {
            releaseAllBitmaps();
            for (DispatchQueue queue : allQueues) {
                queue.cleanupQueue();
                queue.recycle();
            }
            allQueues.clear();
        }

        private static Bitmap getBitmap(String key) {
            if (key == null) {
                return null;
            }
            Bitmap bitmap = bitmapsCache.get(key);
            if (bitmap != null) {
                bitmapsUseCounts.compute(key, (k, count) -> count == null ? 1 : count + 1);
            }
            return bitmap;
        }

        private static void releaseBitmap(String key) {
            if (key == null) {
                return;
            }
            Integer count = bitmapsUseCounts.get(key);
            if (count != null) {
                count--;
                if (count <= 0) {
                    bitmapsUseCounts.remove(key);
                } else {
                    bitmapsUseCounts.put(key, count);
                }
            }
        }

        private static void putBitmap(String key, Bitmap bitmap) {
            if (key == null || bitmap == null) {
                return;
            }
            bitmapsCache.put(key, bitmap);
            bitmapsUseCounts.merge(key, 1, Integer::sum);
        }

        private static void releaseAllBitmaps() {
            bitmapsUseCounts.clear();
            bitmapsCache.evictAll();
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // Option holder
    ///////////////////////////////////////////////////////////////////////////

    static void saveOption(@NonNull MediaPickerOption option) {
        OptionHolder.options.put(option.getId(), option);
    }

    static MediaPickerOption restoredOption(String optionId) {
        return OptionHolder.options.remove(optionId);
    }

    static class OptionHolder {
        private static final Map<String, MediaPickerOption> options = new ArrayMap<>();
    }

}
