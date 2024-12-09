package com.mct.mediapicker.fragment;

import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_NO_MONTH_DAY;
import static android.text.format.DateUtils.FORMAT_SHOW_YEAR;

import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mct.mediapicker.MediaUtils;
import com.mct.mediapicker.R;
import com.mct.mediapicker.adapter.MediaAdapter;
import com.mct.mediapicker.common.dragselect.DragSelectTouchListener;
import com.mct.mediapicker.common.dragselect.DragSelectionProcessor;
import com.mct.mediapicker.common.fastscroll.FastScroller;
import com.mct.mediapicker.common.fastscroll.FastScrollerBuilder;
import com.mct.mediapicker.model.Album;
import com.mct.mediapicker.model.Media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class SetupDataHelper {

    private static final int TAG_DRAG_SELECTION = R.id.tag_drag_selection;
    private static final int TAG_FAST_SCROLLER = R.id.tag_fast_scroller;

    public static void setup(@NonNull RecyclerView rcv,
                             @NonNull Presenter presenter,
                             List<Album> albums,
                             MediaAdapter.OnItemClickListener onItemClickListener,
                             MediaAdapter.OnItemDragListener onItemDragListener) {

        MediaAdapter mediaAdapter = new MediaAdapter(presenter.isMultipleSelect(), albums,
                onItemClickListener,
                onItemDragListener,
                presenter::isSelectedMedia
        );
        GridLayoutManager gridLayoutManager = new GridLayoutManager(rcv.getContext(), 3) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        rcv.setPadding(0, 0, 0, presenter.isMultipleSelect() ? MediaUtils.dp2px(72) : 0);
        rcv.setLayoutManager(gridLayoutManager);
        rcv.setAdapter(mediaAdapter);
        Optional.ofNullable(rcv.getItemAnimator())
                .map(DefaultItemAnimator.class::cast)
                .ifPresent(a -> a.setSupportsChangeAnimations(false));
        attachItemDecoration(rcv);
        attachDragSelection(rcv, mediaAdapter);
        attachFastScroller(rcv, mediaAdapter);
    }

    private static void attachItemDecoration(@NonNull RecyclerView recyclerView) {
        for (int i = 0; i < recyclerView.getItemDecorationCount(); i++) {
            if (recyclerView.getItemDecorationAt(i) instanceof SpacingGridItemDecoration) {
                recyclerView.removeItemDecorationAt(i);
            }
        }
        recyclerView.addItemDecoration(new SpacingGridItemDecoration(3, MediaUtils.dp2px(3), false, 0));
    }

    private static void attachDragSelection(@NonNull RecyclerView recyclerView, @NonNull MediaAdapter mediaAdapter) {
        Object tag;
        if ((tag = recyclerView.getTag(TAG_DRAG_SELECTION)) instanceof DragSelectTouchListener) {
            recyclerView.removeOnItemTouchListener((DragSelectTouchListener) tag);
        }

        DragSelectTouchListener dragSelectTouchListener = new DragSelectTouchListener()
                .withSelectListener(new DragSelectionProcessor(new DragSelectionProcessor.ISelectionHandler() {
                    @Override
                    public Set<Integer> getSelection() {
                        return Collections.emptySet();
                    }

                    @Override
                    public boolean isSelected(int index) {
                        return false;
                    }

                    @Override
                    public void updateSelection(int start, int end, boolean isSelected, boolean calledFromOnStart) {
                        MediaAdapter.OnItemDragListener listener = mediaAdapter.getOnItemDragListener();
                        if (listener != null) {
                            List<Media> media = new ArrayList<>();
                            for (int i = start; i <= end; i++) {
                                media.add(mediaAdapter.getMedia(i));
                            }
                            listener.onDragSelectionChanged(media, isSelected);
                            mediaAdapter.invalidateSelect();
                        }
                    }
                }).withStartFinishedListener(new DragSelectionProcessor.ISelectionStartFinishedListener() {
                    @Override
                    public void onSelectionStarted(int start, boolean originalSelectionState) {
                        recyclerView.getParent().requestDisallowInterceptTouchEvent(true);
                        MediaAdapter.OnItemDragListener listener = mediaAdapter.getOnItemDragListener();
                        if (listener != null) {
                            listener.onStartDrag(start);
                        }
                    }

                    @Override
                    public void onSelectionFinished(int end) {
                        recyclerView.getParent().requestDisallowInterceptTouchEvent(false);
                        MediaAdapter.OnItemDragListener listener = mediaAdapter.getOnItemDragListener();
                        if (listener != null) {
                            listener.onFinishDrag(end);
                        }
                    }
                }))
                // default: 16; - defines the speed of the auto scrolling
                .withMaxScrollDistance(24)
                // default: 0; - set an offset for the touch region on top of the RecyclerView
                .withTopOffset(0)
                // default: 0; - set an offset for the touch region on bottom of the RecyclerView
                .withBottomOffset(-recyclerView.getPaddingBottom())
                // default: true; - enable auto scrolling, even if the finger is moved above the top region
                .withScrollAboveTopRegion(true)
                // default: true; - enable auto scrolling, even if the finger is moved below the top region
                .withScrollBelowTopRegion(true)
                // debug
                .withDebug(false);

        recyclerView.addOnItemTouchListener(dragSelectTouchListener);
        mediaAdapter.setDragSelectTouchListener(dragSelectTouchListener);

        // save drag selection instance
        recyclerView.setTag(TAG_DRAG_SELECTION, dragSelectTouchListener);
    }

    private static void attachFastScroller(@NonNull RecyclerView recyclerView, @NonNull MediaAdapter mediaAdapter) {
        Object tag;
        if ((tag = recyclerView.getTag(TAG_FAST_SCROLLER)) instanceof FastScroller) {
            ((FastScroller) tag).detach();
        }

        FastScroller fastScroller = FastScrollerBuilder.from(recyclerView)
                .setTrackDrawable(R.drawable.mp_ic_fs_track)
                .setThumbDrawable(R.drawable.mp_ic_fs_thumb)
                .setTrackDraggable(false)
                .setScrollOffsetRangeThreshold(400)
                .setPopupTextProvider((view, position) -> Optional.of(mediaAdapter)
                        .map(a -> a.getMedia(position))
                        .map(m -> {
                            int flag = FORMAT_SHOW_YEAR | FORMAT_NO_MONTH_DAY | FORMAT_ABBREV_MONTH;
                            return DateUtils.formatDateTime(view.getContext(), m.getDateModified() * 1000L, flag);
                        })
                        .orElse(null)
                )
                .build();

        fastScroller.attach();

        // save fast scroller instance
        recyclerView.setTag(TAG_FAST_SCROLLER, fastScroller);
    }

}
