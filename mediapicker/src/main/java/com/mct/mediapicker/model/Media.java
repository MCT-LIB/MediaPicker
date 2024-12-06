package com.mct.mediapicker.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.io.Serializable;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class Media implements Serializable {

    private Uri uri;
    private String name;
    private String mimeType;
    private Integer dateModified;
    private Integer size;
    private Integer duration;
    private Integer width;
    private Integer height;

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

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Integer getDateModified() {
        return dateModified;
    }

    public void setDateModified(Integer dateModified) {
        this.dateModified = dateModified;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
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
                ", mimeType=" + mimeType +
                ", dateModified=" + dateModified +
                ", size=" + size +
                ", duration=" + duration +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
