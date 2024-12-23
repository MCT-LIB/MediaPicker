package com.mct.mediapicker.common.fastscroll;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Predicate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

class DefaultViewHelper implements FastScroller.ViewHelper {

    @NonNull
    private final RecyclerView mView;

    @NonNull
    private final Rect mTempRect = new Rect();

    @Nullable
    private RecyclerView.ItemDecoration mItemDecoration;
    @Nullable
    private RecyclerView.OnScrollListener mOnScrollListener;
    @Nullable
    private RecyclerView.SimpleOnItemTouchListener mOnItemTouchListener;

    public DefaultViewHelper(@NonNull RecyclerView view) {
        mView = view;
    }

    @Override
    public void addOnPreDrawListener(@NonNull Runnable onPreDraw) {
        if (mItemDecoration == null) {
            mItemDecoration = new RecyclerView.ItemDecoration() {
                @Override
                public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent,
                                   @NonNull RecyclerView.State state) {
                    onPreDraw.run();
                }
            };
        }
        mView.removeItemDecoration(mItemDecoration);
        mView.addItemDecoration(mItemDecoration);
    }

    @Override
    public void addOnScrollChangedListener(@NonNull Runnable onScrollChanged) {
        if (mOnScrollListener == null) {
            mOnScrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    onScrollChanged.run();
                }
            };
        }
        mView.removeOnScrollListener(mOnScrollListener);
        mView.addOnScrollListener(mOnScrollListener);
    }

    @Override
    public void addOnTouchEventListener(@NonNull Predicate<MotionEvent> onTouchEvent) {
        if (mOnItemTouchListener == null) {
            mOnItemTouchListener = new RecyclerView.SimpleOnItemTouchListener() {
                @Override
                public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView,
                                                     @NonNull MotionEvent event) {
                    return onTouchEvent.test(event);
                }

                @Override
                public void onTouchEvent(@NonNull RecyclerView recyclerView,
                                         @NonNull MotionEvent event) {
                    onTouchEvent.test(event);
                }
            };
        }
        mView.removeOnItemTouchListener(mOnItemTouchListener);
        mView.addOnItemTouchListener(mOnItemTouchListener);
    }

    @Override
    public void clearListeners() {
        if (mItemDecoration != null) {
            mView.removeItemDecoration(mItemDecoration);
            mItemDecoration = null;
        }
        if (mOnScrollListener != null) {
            mView.removeOnScrollListener(mOnScrollListener);
            mOnScrollListener = null;
        }
        if (mOnItemTouchListener != null) {
            mView.removeOnItemTouchListener(mOnItemTouchListener);
            mOnItemTouchListener = null;
        }
    }

    @Override
    public int getScrollRange() {
        int itemCount = getItemCount();
        if (itemCount == 0) {
            return 0;
        }
        int itemHeight = getItemHeight();
        if (itemHeight == 0) {
            return 0;
        }
        return mView.getPaddingTop() + itemCount * itemHeight + mView.getPaddingBottom();
    }

    @Override
    public int getScrollOffset() {
        int firstItemPosition = getFirstItemPosition();
        if (firstItemPosition == RecyclerView.NO_POSITION) {
            return 0;
        }
        int itemHeight = getItemHeight();
        int firstItemTop = getFirstItemOffset();
        return mView.getPaddingTop() + firstItemPosition * itemHeight - firstItemTop;
    }

    @Override
    public void scrollTo(int offset) {
        // Stop any scroll in progress for RecyclerView.
        mView.stopScroll();
        offset -= mView.getPaddingTop();
        int itemHeight = getItemHeight();
        // firstItemPosition should be non-negative even if paddingTop is greater than item height.
        int firstItemPosition = Math.max(0, offset / itemHeight);
        int firstItemTop = firstItemPosition * itemHeight - offset;
        scrollToPositionWithOffset(firstItemPosition, firstItemTop);
    }

    @Override
    public int getItemPosition() {
        return getFirstItemAdapterPosition();
    }

    private int getItemCount() {
        LinearLayoutManager linearLayoutManager = getVerticalLinearLayoutManager();
        if (linearLayoutManager == null) {
            return 0;
        }
        int itemCount = linearLayoutManager.getItemCount();
        if (itemCount == 0) {
            return 0;
        }
        if (linearLayoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) linearLayoutManager;
            itemCount = (itemCount - 1) / gridLayoutManager.getSpanCount() + 1;
        }
        return itemCount;
    }

    private int getItemHeight() {
        if (mView.getChildCount() == 0) {
            return 0;
        }
        View itemView = mView.getChildAt(0);
        mView.getDecoratedBoundsWithMargins(itemView, mTempRect);
        return mTempRect.height();
    }

    private int getFirstItemPosition() {
        int position = getFirstItemAdapterPosition();
        LinearLayoutManager linearLayoutManager = getVerticalLinearLayoutManager();
        if (linearLayoutManager == null) {
            return RecyclerView.NO_POSITION;
        }
        if (linearLayoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) linearLayoutManager;
            position /= gridLayoutManager.getSpanCount();
        }
        return position;
    }

    private int getFirstItemAdapterPosition() {
        if (mView.getChildCount() == 0) {
            return RecyclerView.NO_POSITION;
        }
        View itemView = mView.getChildAt(0);
        LinearLayoutManager linearLayoutManager = getVerticalLinearLayoutManager();
        if (linearLayoutManager == null) {
            return RecyclerView.NO_POSITION;
        }
        return linearLayoutManager.getPosition(itemView);
    }

    private int getFirstItemOffset() {
        if (mView.getChildCount() == 0) {
            return RecyclerView.NO_POSITION;
        }
        View itemView = mView.getChildAt(0);
        mView.getDecoratedBoundsWithMargins(itemView, mTempRect);
        return mTempRect.top;
    }

    private void scrollToPositionWithOffset(int position, int offset) {
        LinearLayoutManager linearLayoutManager = getVerticalLinearLayoutManager();
        if (linearLayoutManager == null) {
            return;
        }
        if (linearLayoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) linearLayoutManager;
            position *= gridLayoutManager.getSpanCount();
        }
        // LinearLayoutManager actually takes offset from paddingTop instead of top of RecyclerView.
        offset -= mView.getPaddingTop();
        linearLayoutManager.scrollToPositionWithOffset(position, offset);
    }

    @Nullable
    private LinearLayoutManager getVerticalLinearLayoutManager() {
        RecyclerView.LayoutManager layoutManager = mView.getLayoutManager();
        if (!(layoutManager instanceof LinearLayoutManager)) {
            return null;
        }
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
        if (linearLayoutManager.getOrientation() != RecyclerView.VERTICAL) {
            return null;
        }
        return linearLayoutManager;
    }
}
