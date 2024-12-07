package com.mct.mediapicker.model;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import java.util.Objects;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class Media implements Parcelable {

    private Uri uri;
    private String name;
    private String mimeType;
    private int dateModified;
    private int size;
    private int duration;
    private int width;
    private int height;

    private String tempDate;

    public Media() {
    }

    protected Media(@NonNull Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
        name = in.readString();
        mimeType = in.readString();
        dateModified = in.readInt();
        size = in.readInt();
        duration = in.readInt();
        width = in.readInt();
        height = in.readInt();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeParcelable(uri, flags);
        dest.writeString(name);
        dest.writeString(mimeType);
        dest.writeInt(dateModified);
        dest.writeInt(size);
        dest.writeInt(duration);
        dest.writeInt(width);
        dest.writeInt(height);
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
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

    public String getTempDate(Context context) {
        if (tempDate == null || tempDate.isEmpty()) {
            tempDate = DateUtils.formatDateTime(context, dateModified * 1000L, DateUtils.FORMAT_SHOW_YEAR);
        }
        return tempDate;
    }

    public void setTempDate(String tempDate) {
        this.tempDate = tempDate;
    }

    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public boolean isVideo() {
        return mimeType != null && mimeType.startsWith("video/");
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Media) {
            Media media = (Media) obj;
            return Objects.equals(uri, media.uri);
        }
        return false;
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
