package com.mct.mediapicker.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import java.util.Objects;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class Media implements Parcelable {

    private int id;
    private int bucketId;
    private String path;
    private String name;
    private String mimeType;
    private int dateModified;
    private int size;
    private int width;
    private int height;
    private int duration;

    public Media() {
    }

    protected Media(@NonNull Parcel in) {
        id = in.readInt();
        bucketId = in.readInt();
        path = in.readString();
        name = in.readString();
        mimeType = in.readString();
        dateModified = in.readInt();
        size = in.readInt();
        width = in.readInt();
        height = in.readInt();
        duration = in.readInt();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(bucketId);
        dest.writeString(path);
        dest.writeString(name);
        dest.writeString(mimeType);
        dest.writeInt(dateModified);
        dest.writeInt(size);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeInt(duration);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Media> CREATOR = new Creator<Media>() {
        // @formatter:off
        public @NonNull Media createFromParcel(Parcel in) {return new Media(in);}
        public @NonNull Media[] newArray(int size) {return new Media[size];}
        // @formatter:on
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBucketId() {
        return bucketId;
    }

    public void setBucketId(int bucketId) {
        this.bucketId = bucketId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public int getDateModified() {
        return dateModified;
    }

    public void setDateModified(int dateModified) {
        this.dateModified = dateModified;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public boolean isVideo() {
        return mimeType != null && mimeType.startsWith("video/");
    }

    public Uri getUri() {
        return Uri.parse("file://" + path);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Media) {
            Media media = (Media) obj;
            return Objects.equals(path, media.path);
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return "Media{" +
                "id=" + id +
                ", bucketId=" + bucketId +
                ", path=" + path +
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
