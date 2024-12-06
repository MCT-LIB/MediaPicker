package com.mct.mediapicker.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.io.Serializable;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class Media implements Serializable {

    private Uri uri;
    private String name;
    private Long size;
    private String mimeType;
    private Long dateModified;

    public Media() {
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getDateModified() {
        return dateModified;
    }

    public void setDateModified(Long dateModified) {
        this.dateModified = dateModified;
    }

    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public boolean isVideo() {
        return mimeType != null && mimeType.startsWith("video/");
    }

    @NonNull
    @Override
    public String toString() {
        return "Media{" +
                "uri=" + uri +
                ", name=" + name +
                ", size=" + size +
                ", mimeType=" + mimeType +
                '}';
    }
}
