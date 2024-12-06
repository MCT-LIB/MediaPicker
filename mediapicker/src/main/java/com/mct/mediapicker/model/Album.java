package com.mct.mediapicker.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class Album implements Serializable {

    private String bucketId;
    private String bucketName;
    private final List<Media> mediaList;

    public Album(String bucketId, String bucketName) {
        this.bucketId = bucketId;
        this.bucketName = bucketName;
        this.mediaList = new ArrayList<>();
    }

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
