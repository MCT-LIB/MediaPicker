package com.mct.mediapicker.common.fastscroll;

import android.graphics.Rect;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.mct.mediapicker.R;

public class FastScrollerBuilder {

    @NonNull
    public static FastScrollerBuilder from(@NonNull RecyclerView view) {
        return new FastScrollerBuilder(view);
    }

    @NonNull
    final RecyclerView mRecyclerView;

    @NonNull
    Integer mThumbDrawableRes;

    @NonNull
    Integer mTrackDrawableRes;

    @NonNull
    Boolean mTrackDraggable = Boolean.TRUE;

    @NonNull
    Integer mScrollOffsetRangeThreshold = 0;

    @Nullable
    Rect mPadding;

    @Nullable
    FastScroller.ViewHelper mViewHelper;

    @Nullable
    FastScroller.AnimationHelper mAnimationHelper;

    @Nullable
    FastScroller.PopupStyleHelper mPopupStyleHelper;

    @Nullable
    FastScroller.PopupTextProvider mPopupTextProvider;

    @Nullable
    FastScroller.DraggingListener mDraggingListener;

    private FastScrollerBuilder(@NonNull RecyclerView view) {
        mRecyclerView = view;
        mThumbDrawableRes = R.drawable.fs_thumb;
        mTrackDrawableRes = R.drawable.fs_track;
    }

    @NonNull
    public FastScrollerBuilder setThumbDrawable(@DrawableRes int thumbDrawableRes) {
        mThumbDrawableRes = thumbDrawableRes;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setTrackDrawable(@DrawableRes int trackDrawableRes) {
        mTrackDrawableRes = trackDrawableRes;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setTrackDraggable(boolean trackDraggable) {
        mTrackDraggable = trackDraggable;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setScrollOffsetRangeThreshold(int scrollOffsetRangeThreshold) {
        mScrollOffsetRangeThreshold = scrollOffsetRangeThreshold;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setPadding(int left, int top, int right, int bottom) {
        if (mPadding == null) {
            mPadding = new Rect();
        }
        mPadding.set(left, top, right, bottom);
        return this;
    }

    @NonNull
    public FastScrollerBuilder setPadding(@Nullable Rect padding) {
        if (padding != null) {
            if (mPadding == null) {
                mPadding = new Rect();
            }
            mPadding.set(padding);
        } else {
            mPadding = null;
        }
        return this;
    }

    public FastScrollerBuilder setViewHelper(@Nullable FastScroller.ViewHelper viewHelper) {
        mViewHelper = viewHelper;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setAnimationHelper(@Nullable FastScroller.AnimationHelper animationHelper) {
        mAnimationHelper = animationHelper;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setPopupStyle(@Nullable FastScroller.PopupStyleHelper popupStyle) {
        mPopupStyleHelper = popupStyle;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setPopupTextProvider(@Nullable FastScroller.PopupTextProvider popupTextProvider) {
        mPopupTextProvider = popupTextProvider;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setDraggingListener(@Nullable FastScroller.DraggingListener draggingListener) {
        mDraggingListener = draggingListener;
        return this;
    }

    @NonNull
    public FastScroller build() {
        return new FastScroller(
                mRecyclerView,
                mThumbDrawableRes,
                mTrackDrawableRes,
                mTrackDraggable,
                mScrollOffsetRangeThreshold,
                mPadding,
                mViewHelper,
                mAnimationHelper,
                mPopupStyleHelper,
                mPopupTextProvider,
                mDraggingListener
        );
    }

}
