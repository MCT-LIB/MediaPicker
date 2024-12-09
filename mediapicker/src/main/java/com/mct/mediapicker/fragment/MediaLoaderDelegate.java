package com.mct.mediapicker.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.mct.mediapicker.model.Media;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface MediaLoaderDelegate {
    void onAttach(View view);

    void onDetach(View view);

    void loadThumbnail(Media media);

    void setListener(MediaLoaderListener listener);

    interface MediaLoaderListener {

        void onThumbnailLoaded(Bitmap bitmap);
    }

    @NonNull
    static MediaLoaderDelegate create(Context context) {
        return Presenter.create(context, false);
    }

    @NonNull
    static MediaLoaderDelegate create(Context context, boolean fullScreen) {
        return Presenter.create(context, fullScreen);
    }
}
