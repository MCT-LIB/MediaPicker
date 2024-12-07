package com.mct.mediapicker.common.fastscroll;

import android.graphics.Rect;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.mct.mediapicker.R;

public class FastScrollerBuilder {

    @NonNull
    private final ViewGroup mView;

    @Nullable
    private FastScroller.ViewHelper mViewHelper;

    @Nullable
    private PopupTextProvider mPopupTextProvider;

    @Nullable
    private Rect mPadding;

    @Nullable
    private Integer mScrollOffsetRangeThreshold;

    @Nullable
    private Boolean mTrackDraggable = Boolean.TRUE;

    @NonNull
    private Integer mTrackDrawableRes;

    @NonNull
    private Integer mThumbDrawableRes;

    @NonNull
    private Consumer<TextView> mPopupStyle;

    @Nullable
    private FastScroller.AnimationHelper mAnimationHelper;

    @Nullable
    private FastScroller.DraggingListener mDraggingListener;

    public FastScrollerBuilder(@NonNull ViewGroup view) {
        mView = view;
        mTrackDrawableRes = R.drawable.afs_md2_track;
        mThumbDrawableRes = R.drawable.afs_md2_thumb;
        mPopupStyle = PopupStyles.MD2;
    }

    @NonNull
    public FastScrollerBuilder setViewHelper(@Nullable FastScroller.ViewHelper viewHelper) {
        mViewHelper = viewHelper;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setPopupTextProvider(@Nullable PopupTextProvider popupTextProvider) {
        mPopupTextProvider = popupTextProvider;
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

    @NonNull
    public FastScrollerBuilder setScrollOffsetRangeThreshold(int scrollOffsetRangeThreshold) {
        mScrollOffsetRangeThreshold = scrollOffsetRangeThreshold;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setTrackDraggable(boolean trackDraggable) {
        mTrackDraggable = trackDraggable;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setTrackDrawable(@DrawableRes int trackDrawableRes) {
        mTrackDrawableRes = trackDrawableRes;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setThumbDrawable(@DrawableRes int thumbDrawableRes) {
        mThumbDrawableRes = thumbDrawableRes;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setPopupStyle(@NonNull Consumer<TextView> popupStyle) {
        mPopupStyle = popupStyle;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setAnimationHelper(@Nullable FastScroller.AnimationHelper animationHelper) {
        mAnimationHelper = animationHelper;
        return this;
    }

    public void disableScrollbarAutoHide() {
        DefaultAnimationHelper animationHelper = new DefaultAnimationHelper(mView);
        animationHelper.setScrollbarAutoHideEnabled(false);
        mAnimationHelper = animationHelper;
    }

    @NonNull
    public FastScrollerBuilder setDraggingListener(@Nullable FastScroller.DraggingListener draggingListener) {
        mDraggingListener = draggingListener;
        return this;
    }

    @NonNull
    public FastScroller build() {
        return new FastScroller(mView, getOrCreateViewHelper(),
                mPadding, mScrollOffsetRangeThreshold,
                mTrackDraggable, mTrackDrawableRes, mThumbDrawableRes,
                mPopupStyle,
                getOrCreateAnimationHelper(),
                mDraggingListener
        );
    }

    @NonNull
    private FastScroller.ViewHelper getOrCreateViewHelper() {
        if (mViewHelper != null) {
            return mViewHelper;
        }
        if (mView instanceof ViewHelperProvider) {
            return ((ViewHelperProvider) mView).getViewHelper();
        } else if (mView instanceof RecyclerView) {
            return new RecyclerViewHelper((RecyclerView) mView, mPopupTextProvider);
        } else {
            throw new UnsupportedOperationException(mView.getClass().getSimpleName()
                    + " is not supported for fast scroll");
        }
    }

    @NonNull
    private FastScroller.AnimationHelper getOrCreateAnimationHelper() {
        if (mAnimationHelper != null) {
            return mAnimationHelper;
        }
        return new DefaultAnimationHelper(mView);
    }
}
