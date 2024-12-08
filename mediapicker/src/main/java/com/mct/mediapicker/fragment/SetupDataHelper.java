package com.mct.mediapicker.fragment;

import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_NO_MONTH_DAY;
import static android.text.format.DateUtils.FORMAT_SHOW_YEAR;

import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.core.util.Supplier;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mct.mediapicker.MediaUtils;
import com.mct.mediapicker.R;
import com.mct.mediapicker.adapter.MediaAdapter;
import com.mct.mediapicker.common.fastscroll.FastScrollerBuilder;
import com.mct.mediapicker.model.Album;

import java.util.List;
import java.util.Optional;

class SetupDataHelper {

    private static final int MIN_SPAN_COUNT = 3;
    private static final int MAX_SPAN_COUNT = 9;
    private static final int TAG_FAST_SCROLLER = R.id.tag_fast_scroller;

    public static void setup(@NonNull RecyclerView rcv, @NonNull Presenter presenter, List<Album> albums, MediaAdapter.OnItemClickListener listener) {
        MediaAdapter mediaAdapter = new MediaAdapter(presenter.isMultipleSelect(), albums, listener, presenter::isSelectedMedia);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(rcv.getContext(), MIN_SPAN_COUNT);
        rcv.setPadding(0, 0, 0, presenter.isMultipleSelect() ? MediaUtils.dp2px(72) : 0);
        rcv.setLayoutManager(gridLayoutManager);
        rcv.setAdapter(mediaAdapter);
        attachItemDecoration(rcv);
        attachZoomGesture(rcv);
        attachFastScroller(rcv);
    }

    private static void attachZoomGesture(@NonNull RecyclerView rcv) {

    }

    private static void attachItemDecoration(@NonNull RecyclerView rcv) {
        for (int i = 0; i < rcv.getItemDecorationCount(); i++) {
            if (rcv.getItemDecorationAt(i) instanceof SpacingGridItemDecoration) {
                rcv.removeItemDecorationAt(i);
            }
        }
        rcv.addItemDecoration(new SpacingGridItemDecoration(3, MediaUtils.dp2px(3), false, 0));
    }

    private static void attachFastScroller(@NonNull RecyclerView recyclerView) {
        if (recyclerView.getTag(TAG_FAST_SCROLLER) != null) {
            return;
        }
        recyclerView.setTag(TAG_FAST_SCROLLER, 1);

        Supplier<Optional<MediaAdapter>> adapterSupplier = () -> Optional.of(recyclerView)
                .map(RecyclerView::getAdapter)
                .filter(MediaAdapter.class::isInstance)
                .map(MediaAdapter.class::cast);

        FastScrollerBuilder.from(recyclerView)
                .setTrackDrawable(R.drawable.mp_ic_fs_track)
                .setThumbDrawable(R.drawable.mp_ic_fs_thumb)
                .setTrackDraggable(false)
                .setScrollOffsetRangeThreshold(400)
                .setDraggingListener(dragging -> adapterSupplier.get().ifPresent(a -> a.setDragging(dragging)))
                .setPopupTextProvider((view, position) -> adapterSupplier.get()
                        .map(a -> a.getMedia(position))
                        .map(m -> {
                            int flag = FORMAT_SHOW_YEAR | FORMAT_NO_MONTH_DAY | FORMAT_ABBREV_MONTH;
                            return DateUtils.formatDateTime(view.getContext(), m.getDateModified() * 1000L, flag);
                        })
                        .orElse(null)
                )
                .build()
                .attach();
    }

}
