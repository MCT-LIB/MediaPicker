package com.mct.mediapicker.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.util.ArrayList;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class Album implements Parcelable {

    private String bucketId;
    private String bucketName;
    private final List<Media> mediaList;

    public Album(String bucketId, String bucketName) {
        this.bucketId = bucketId;
        this.bucketName = bucketName;
        this.mediaList = new ArrayList<>();
    }

    protected Album(@NonNull Parcel in) {
        bucketId = in.readString();
        bucketName = in.readString();
        mediaList = in.createTypedArrayList(Media.CREATOR);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(bucketId);
        dest.writeString(bucketName);
        dest.writeTypedList(mediaList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        // @formatter:off
        public @NonNull Album createFromParcel(Parcel in) {return new Album(in);}
        public @NonNull Album[] newArray(int size) {return new Album[size];}
        // @formatter:on
    };

    public String getBucketId() {
        return bucketId;
    }

    public void setBucketId(String bucketId) {
        this.bucketId = bucketId;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public Uri getBucketThumbUri() {
        return mediaList.isEmpty() ? null : mediaList.get(mediaList.size() - 1).getUri();
    }

    public List<Media> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<Media> mediaList) {
        this.mediaList.clear();
        this.mediaList.addAll(mediaList);
    }

    public void addMedia(@NonNull Media media) {
        mediaList.add(media);
    }

    @NonNull
    @Override
    public String toString() {
        return "Album{" + "bucketId=" + bucketId + ", bucketName=" + bucketName + ", mediaList=" + mediaList + '}';
    }
}
