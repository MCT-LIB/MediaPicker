package com.mct.mediapicker.common.fastscroll;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.math.MathUtils;
import androidx.core.util.Predicate;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;
import java.util.Optional;

public class FastScroller {

    private static final int MIN_TOUCH_TARGET_SIZE = Utils.dp2px(48);

    private final int mTouchSlop;

    @NonNull
    private final RecyclerView mView;
    private final boolean mTrackDraggable;
    private final int mScrollOffsetRangeThreshold;
    @Nullable
    private Rect mUserPadding;
    @NonNull
    private final FastScroller.ViewHelper mViewHelper;
    @NonNull
    private final FastScroller.AnimationHelper mAnimationHelper;
    @Nullable
    private final FastScroller.PopupStyleHelper mPopupStyleHelper;
    @Nullable
    private final FastScroller.PopupTextProvider mPopupTextProvider;
    @Nullable
    private final FastScroller.DraggingListener mDraggingListener;

    @NonNull
    private final View mThumbView;
    @NonNull
    private final View mTrackView;
    @NonNull
    private final TextView mPopupView;

    private int mTrackWidth;
    private int mThumbWidth;
    private int mThumbHeight;
    private int mThumbOffset;
    private boolean mScrollbarEnabled;
    private boolean mAttached;

    private float mDownX;
    private float mDownY;
    private float mLastY;
    private float mDragStartY;
    private int mDragStartThumbOffset;
    private boolean mDragging;

    @NonNull
    private final Rect mTempRect = new Rect();

    public FastScroller(@NonNull RecyclerView recyclerView,
                        int thumbDrawableRes,
                        int trackDrawableRes,
                        boolean trackDraggable,
                        int scrollOffsetRangeThreshold,
                        @Nullable Rect userPadding,
                        @Nullable ViewHelper viewHelper,
                        @Nullable AnimationHelper animationHelper,
                        @Nullable PopupStyleHelper popupStyleHelper,
                        @Nullable PopupTextProvider popupTextProvider,
                        @Nullable DraggingListener draggingListener) {

        Context context = recyclerView.getContext();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mView = recyclerView;
        mUserPadding = userPadding;
        mTrackDraggable = trackDraggable;
        mScrollOffsetRangeThreshold = scrollOffsetRangeThreshold;

        mViewHelper = Optional.ofNullable(viewHelper).orElseGet(() -> new DefaultViewHelper(recyclerView));
        mAnimationHelper = Optional.ofNullable(animationHelper).orElseGet(() -> new DefaultAnimationHelper(recyclerView));
        mPopupStyleHelper = popupStyleHelper;
        mPopupTextProvider = popupTextProvider;
        mDraggingListener = draggingListener;

        mThumbView = new View(context);
        mTrackView = new View(context);
        mPopupView = new AppCompatTextView(context);

        initScrollbar(context, thumbDrawableRes, trackDrawableRes);
        postAutoHideScrollbar();
    }

    private void initScrollbar(Context context, int thumbDrawableRes, int trackDrawableRes) {
        Drawable thumbDrawable = Objects.requireNonNull(ContextCompat.getDrawable(context, thumbDrawableRes));
        Drawable trackDrawable = Objects.requireNonNull(ContextCompat.getDrawable(context, trackDrawableRes));

        mThumbWidth = requireNonNegative(thumbDrawable.getIntrinsicWidth(), "thumbDrawable.getIntrinsicWidth() < 0");
        mThumbHeight = requireNonNegative(thumbDrawable.getIntrinsicHeight(), "thumbDrawable.getIntrinsicHeight() < 0");
        mTrackWidth = requireNonNegative(trackDrawable.getIntrinsicWidth(), "trackDrawable.getIntrinsicWidth() < 0");

        mThumbView.setBackground(thumbDrawable);
        mTrackView.setBackground(trackDrawable);
        mPopupView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        );
        new DefaultPopupStyleHelper().apply(mPopupView);
        if (mPopupStyleHelper != null) {
            mPopupStyleHelper.apply(mPopupView);
        }
        mPopupView.setAlpha(0);
    }

    private static int requireNonNegative(int value, @NonNull String message) {
        if (value < 0) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public void attach() {
        if (mAttached) {
            return;
        }
        mAttached = true;
        ViewGroupOverlay overlay = mView.getOverlay();
        overlay.add(mThumbView);
        overlay.add(mTrackView);
        overlay.add(mPopupView);

        mViewHelper.addOnPreDrawListener(this::onPreDraw);
        mViewHelper.addOnScrollChangedListener(this::onScrollChanged);
        mViewHelper.addOnTouchEventListener(this::onTouchEvent);
    }

    public void detach() {
        if (!mAttached) {
            return;
        }
        mAttached = false;
        ViewGroupOverlay overlay = mView.getOverlay();
        overlay.remove(mThumbView);
        overlay.remove(mTrackView);
        overlay.remove(mPopupView);

        mViewHelper.clearListeners();
    }

    public void setPadding(int left, int top, int right, int bottom) {
        if (mUserPadding != null && mUserPadding.left == left && mUserPadding.top == top
                && mUserPadding.right == right && mUserPadding.bottom == bottom) {
            return;
        }
        if (mUserPadding == null) {
            mUserPadding = new Rect();
        }
        mUserPadding.set(left, top, right, bottom);
        mView.invalidate();
    }

    public void setPadding(@Nullable Rect padding) {
        if (Objects.equals(mUserPadding, padding)) {
            return;
        }
        if (padding != null) {
            if (mUserPadding == null) {
                mUserPadding = new Rect();
            }
            mUserPadding.set(padding);
        } else {
            mUserPadding = null;
        }
        mView.invalidate();
    }

    @NonNull
    private Rect getPadding() {
        if (mUserPadding != null) {
            mTempRect.set(mUserPadding);
        } else {
            mTempRect.set(
                    mView.getPaddingLeft(),
                    mView.getPaddingTop(),
                    mView.getPaddingRight(),
                    mView.getPaddingBottom()
            );
        }
        return mTempRect;
    }

    private void onPreDraw() {

        updateScrollbarState();
        mTrackView.setVisibility(mScrollbarEnabled ? View.VISIBLE : View.INVISIBLE);
        mThumbView.setVisibility(mScrollbarEnabled ? View.VISIBLE : View.INVISIBLE);
        if (!mScrollbarEnabled) {
            mPopupView.setVisibility(View.INVISIBLE);
            return;
        }

        int layoutDirection = mView.getLayoutDirection();
        mTrackView.setLayoutDirection(layoutDirection);
        mThumbView.setLayoutDirection(layoutDirection);
        mPopupView.setLayoutDirection(layoutDirection);

        boolean isLayoutRtl = layoutDirection == View.LAYOUT_DIRECTION_RTL;
        int viewWidth = mView.getWidth();
        int viewHeight = mView.getHeight();

        Rect padding = getPadding();
        int trackLeft = isLayoutRtl ? padding.left : viewWidth - padding.right - mTrackWidth;
        layoutView(mTrackView, trackLeft, padding.top, trackLeft + mTrackWidth,
                Math.max(viewHeight - padding.bottom, padding.top));
        int thumbLeft = isLayoutRtl ? padding.left : viewWidth - padding.right - mThumbWidth;
        int thumbTop = padding.top + mThumbOffset;
        layoutView(mThumbView, thumbLeft, thumbTop, thumbLeft + mThumbWidth,
                thumbTop + mThumbHeight);

        CharSequence popupText;
        if (mPopupTextProvider != null) {
            popupText = mPopupTextProvider.getPopupText(mView, mViewHelper.getItemPosition());
        } else {
            popupText = null;
        }
        boolean hasPopup = !TextUtils.isEmpty(popupText);
        mPopupView.setVisibility(hasPopup ? View.VISIBLE : View.INVISIBLE);
        if (hasPopup) {
            FrameLayout.LayoutParams popupLayoutParams = (FrameLayout.LayoutParams)
                    mPopupView.getLayoutParams();
            if (!Objects.equals(mPopupView.getText(), popupText)) {
                mPopupView.setText(popupText);
                int widthMeasureSpec = ViewGroup.getChildMeasureSpec(
                        View.MeasureSpec.makeMeasureSpec(viewWidth, View.MeasureSpec.EXACTLY),
                        padding.left + padding.right + mThumbWidth + popupLayoutParams.leftMargin
                                + popupLayoutParams.rightMargin, popupLayoutParams.width);
                int heightMeasureSpec = ViewGroup.getChildMeasureSpec(
                        View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY),
                        padding.top + padding.bottom + popupLayoutParams.topMargin
                                + popupLayoutParams.bottomMargin, popupLayoutParams.height);
                mPopupView.measure(widthMeasureSpec, heightMeasureSpec);
            }
            int popupWidth = mPopupView.getMeasuredWidth();
            int popupHeight = mPopupView.getMeasuredHeight();
            int popupLeft = isLayoutRtl ? padding.left + mThumbWidth + popupLayoutParams.leftMargin
                    : viewWidth - padding.right - mThumbWidth - popupLayoutParams.rightMargin
                    - popupWidth;
            int popupAnchorY;
            switch (popupLayoutParams.gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.LEFT:
                default:
                    popupAnchorY = 0;
                    break;
                case Gravity.CENTER_HORIZONTAL:
                    popupAnchorY = popupHeight / 2;
                    break;
                case Gravity.RIGHT:
                    popupAnchorY = popupHeight;
                    break;
            }
            int thumbAnchorY;
            switch (popupLayoutParams.gravity & Gravity.VERTICAL_GRAVITY_MASK) {
                case Gravity.TOP:
                default:
                    thumbAnchorY = mThumbView.getPaddingTop();
                    break;
                case Gravity.CENTER_VERTICAL: {
                    int thumbPaddingTop = mThumbView.getPaddingTop();
                    thumbAnchorY = thumbPaddingTop + (mThumbHeight - thumbPaddingTop
                            - mThumbView.getPaddingBottom()) / 2;
                    break;
                }
                case Gravity.BOTTOM:
                    thumbAnchorY = mThumbHeight - mThumbView.getPaddingBottom();
                    break;
            }
            int popupTop = MathUtils.clamp(thumbTop + thumbAnchorY - popupAnchorY,
                    padding.top + popupLayoutParams.topMargin,
                    viewHeight - padding.bottom - popupLayoutParams.bottomMargin - popupHeight);
            layoutView(mPopupView, popupLeft, popupTop, popupLeft + popupWidth,
                    popupTop + popupHeight);
        }
    }

    private void updateScrollbarState() {
        int scrollOffsetRange = getScrollOffsetRange();
        mScrollbarEnabled = scrollOffsetRange > mScrollOffsetRangeThreshold;
        mThumbOffset = mScrollbarEnabled ? (int) ((long) getThumbOffsetRange()
                * mViewHelper.getScrollOffset() / scrollOffsetRange) : 0;
    }

    private void layoutView(@NonNull View view, int left, int top, int right, int bottom) {
        int scrollX = mView.getScrollX();
        int scrollY = mView.getScrollY();
        view.layout(scrollX + left, scrollY + top, scrollX + right, scrollY + bottom);
    }

    private void onScrollChanged() {

        updateScrollbarState();
        if (!mScrollbarEnabled) {
            return;
        }

        mAnimationHelper.showScrollbar(mTrackView, mThumbView);
        mAnimationHelper.showPopup(mPopupView);
        postAutoHideScrollbar();
        postAutoHidePopup();
    }

    private boolean onTouchEvent(@NonNull MotionEvent event) {

        if (!mScrollbarEnabled) {
            return false;
        }

        float eventX = event.getX();
        float eventY = event.getY();
        Rect padding = getPadding();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                mDownX = eventX;
                mDownY = eventY;

                if (mThumbView.getAlpha() > 0 && isInViewTouchTarget(mThumbView, eventX, eventY)) {
                    mDragStartY = eventY;
                    mDragStartThumbOffset = mThumbOffset;
                    setDragging(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:

                if (!mDragging && mTrackDraggable && isInViewTouchTarget(mTrackView, mDownX, mDownY)
                        && Math.abs(eventY - mDownY) > mTouchSlop) {
                    if (isInViewTouchTarget(mThumbView, mDownX, mDownY)) {
                        mDragStartY = mLastY;
                        mDragStartThumbOffset = mThumbOffset;
                    } else {
                        mDragStartY = eventY;
                        mDragStartThumbOffset = (int) (eventY - padding.top - mThumbHeight / 2f);
                        scrollToThumbOffset(mDragStartThumbOffset);
                    }
                    setDragging(true);
                }

                if (mDragging) {
                    int thumbOffset = mDragStartThumbOffset + (int) (eventY - mDragStartY);
                    scrollToThumbOffset(thumbOffset);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                setDragging(false);
                break;
        }

        mLastY = eventY;

        return mDragging;
    }

    private boolean isInView(@NonNull View view, float x, float y) {
        int scrollX = mView.getScrollX();
        int scrollY = mView.getScrollY();
        return x >= view.getLeft() - scrollX && x < view.getRight() - scrollX
                && y >= view.getTop() - scrollY && y < view.getBottom() - scrollY;
    }

    private boolean isInViewTouchTarget(@NonNull View view, float x, float y) {
        int scrollX = mView.getScrollX();
        int scrollY = mView.getScrollY();
        return isInTouchTarget(x, view.getLeft() - scrollX, view.getRight() - scrollX, 0, mView.getWidth())
                && isInTouchTarget(y, view.getTop() - scrollY, view.getBottom() - scrollY, 0, mView.getHeight());
    }

    private boolean isInTouchTarget(float position, int viewStart, int viewEnd, int parentStart,
                                    int parentEnd) {
        int viewSize = viewEnd - viewStart;
        if (viewSize >= MIN_TOUCH_TARGET_SIZE) {
            return position >= viewStart && position < viewEnd;
        }
        int touchTargetStart = viewStart - (MIN_TOUCH_TARGET_SIZE - viewSize) / 2;
        if (touchTargetStart < parentStart) {
            touchTargetStart = parentStart;
        }
        int touchTargetEnd = touchTargetStart + MIN_TOUCH_TARGET_SIZE;
        if (touchTargetEnd > parentEnd) {
            touchTargetEnd = parentEnd;
            touchTargetStart = touchTargetEnd - MIN_TOUCH_TARGET_SIZE;
            if (touchTargetStart < parentStart) {
                touchTargetStart = parentStart;
            }
        }
        return position >= touchTargetStart && position < touchTargetEnd;
    }

    private void scrollToThumbOffset(int thumbOffset) {
        int thumbOffsetRange = getThumbOffsetRange();
        thumbOffset = MathUtils.clamp(thumbOffset, 0, thumbOffsetRange);
        int scrollOffset = (int) ((long) getScrollOffsetRange() * thumbOffset / thumbOffsetRange);
        mViewHelper.scrollTo(scrollOffset);
    }

    private int getScrollOffsetRange() {
        return mViewHelper.getScrollRange() - mView.getHeight();
    }

    private int getThumbOffsetRange() {
        Rect padding = getPadding();
        return mView.getHeight() - padding.top - padding.bottom - mThumbHeight;
    }

    private void setDragging(boolean dragging) {

        if (mDragging == dragging) {
            return;
        }
        mDragging = dragging;

        if (mDragging) {
            mView.getParent().requestDisallowInterceptTouchEvent(true);
        }

        mTrackView.setPressed(mDragging);
        mThumbView.setPressed(mDragging);

        if (mDraggingListener != null) {
            mDraggingListener.onDragging(mDragging);
        }

        if (mDragging) {
            mView.removeCallbacks(mAutoHideScrollbarRunnable);
            mView.removeCallbacks(mAutoHidePopupRunnable);
            mAnimationHelper.showScrollbar(mTrackView, mThumbView);
            mAnimationHelper.showPopup(mPopupView);
        } else {
            postAutoHideScrollbar();
            postAutoHidePopup();
        }
    }

    private final Runnable mAutoHideScrollbarRunnable = this::autoHideScrollbar;
    private final Runnable mAutoHidePopupRunnable = this::autoHidePopup;

    private void autoHideScrollbar() {
        if (mDragging) {
            return;
        }
        mAnimationHelper.hideScrollbar(mTrackView, mThumbView);
    }

    private void autoHidePopup() {
        if (mDragging) {
            return;
        }
        mAnimationHelper.hidePopup(mPopupView);
    }

    private void postAutoHideScrollbar() {
        mView.removeCallbacks(mAutoHideScrollbarRunnable);
        if (mAnimationHelper.isScrollbarAutoHideEnabled()) {
            mView.postDelayed(mAutoHideScrollbarRunnable, mAnimationHelper.getScrollbarAutoHideDelayMillis());
        }
    }

    private void postAutoHidePopup() {
        mView.removeCallbacks(mAutoHidePopupRunnable);
        mView.postDelayed(mAutoHidePopupRunnable, mAnimationHelper.getPopupAutoHideDelayMillis());
    }

    public interface ViewHelper {

        void addOnPreDrawListener(@NonNull Runnable onPreDraw);

        void addOnScrollChangedListener(@NonNull Runnable onScrollChanged);

        void addOnTouchEventListener(@NonNull Predicate<MotionEvent> onTouchEvent);

        void clearListeners();

        int getScrollRange();

        int getScrollOffset();

        void scrollTo(int offset);


        default int getItemPosition() {
            return -1;
        }
    }

    public interface AnimationHelper {

        void showScrollbar(@NonNull View trackView, @NonNull View thumbView);

        void hideScrollbar(@NonNull View trackView, @NonNull View thumbView);

        boolean isScrollbarAutoHideEnabled();

        int getScrollbarAutoHideDelayMillis();

        void showPopup(@NonNull View popupView);

        void hidePopup(@NonNull View popupView);

        int getPopupAutoHideDelayMillis();
    }

    public interface PopupStyleHelper {

        void apply(@NonNull TextView popupView);
    }

    public interface PopupTextProvider {

        String getPopupText(@NonNull RecyclerView view, int position);
    }

    public interface DraggingListener {

        void onDragging(boolean dragging);
    }
}
