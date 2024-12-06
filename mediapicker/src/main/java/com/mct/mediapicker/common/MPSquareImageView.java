package com.mct.mediapicker.common;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.imageview.ShapeableImageView;

public class MPSquareImageView extends ShapeableImageView {

    public MPSquareImageView(@NonNull Context context) {
        super(context);
    }

    public MPSquareImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MPSquareImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int size, int heightMeasureSpec) {
        super.onMeasure(size, size);
    }
}
