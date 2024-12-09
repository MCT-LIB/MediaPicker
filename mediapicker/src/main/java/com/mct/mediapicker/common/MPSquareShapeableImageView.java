package com.mct.mediapicker.common;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.imageview.ShapeableImageView;

public class MPSquareShapeableImageView extends ShapeableImageView {

    public MPSquareShapeableImageView(@NonNull Context context) {
        super(context);
    }

    public MPSquareShapeableImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MPSquareShapeableImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int size, int heightMeasureSpec) {
        super.onMeasure(size, size);
    }
}
