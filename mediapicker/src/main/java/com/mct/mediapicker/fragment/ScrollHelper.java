package com.mct.mediapicker.fragment;

import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mct.mediapicker.MediaUtils;
import com.mct.mediapicker.R;
import com.mct.mediapicker.adapter.MediaAdapter;
import com.mct.mediapicker.common.fastscroll.FastScroller;
import com.mct.mediapicker.common.fastscroll.FastScrollerBuilder;
import com.mct.mediapicker.common.fastscroll.PopupStyles;

import java.util.Optional;

class ScrollHelper {

    private static final int TAG_FAST_SCROLLER = R.id.tag_fast_scroller;

    public static void attachTo(@NonNull RecyclerView recyclerView) {
        if (!(recyclerView.getAdapter() instanceof MediaAdapter)) {
            return;
        }
        if (recyclerView.getTag(TAG_FAST_SCROLLER) instanceof FastScroller) {
            recyclerView.invalidateItemDecorations();
            return;
        }
        FastScroller scroller = new FastScrollerBuilder(recyclerView)
                .setScrollOffsetRangeThreshold(400)
                .setTrackDraggable(false)
                .setDraggingListener(dragging -> getAdapter(recyclerView).ifPresent(a -> a.setDragging(dragging)))
                .setTrackDrawable(R.drawable.mp_ic_fs_track)
                .setThumbDrawable(R.drawable.mp_ic_fs_thumb)
                .setPopupStyle(popupView -> {
                    PopupStyles.MD2.accept(popupView);
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) popupView.getLayoutParams();
                    layoutParams.gravity = Gravity.CENTER;
                    layoutParams.setMarginEnd(MediaUtils.dp2px(8));
                    popupView.setLayoutParams(layoutParams);
                    int padStart = MediaUtils.dp2px(12);
                    int padEnd = MediaUtils.dp2px(20);
                    int padVertical = MediaUtils.dp2px(8);
                    popupView.setPaddingRelative(padStart, padVertical, padEnd, padVertical);
                    popupView.setMinWidth(0);
                    popupView.setMinimumHeight(0);
                    popupView.setTextSize(14);
                    popupView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                })
                .setPopupTextProvider((view, position) -> getAdapter(recyclerView)
                        .map(a -> a.getMedia(position))
                        .map(m -> m.getTempDate(view.getContext()))
                        .orElse(null)
                ).build();

        recyclerView.setTag(TAG_FAST_SCROLLER, scroller);
    }

    private static Optional<MediaAdapter> getAdapter(@NonNull RecyclerView recyclerView) {
        return Optional.ofNullable(recyclerView.getAdapter())
                .filter(MediaAdapter.class::isInstance)
                .map(MediaAdapter.class::cast);
    }
}
