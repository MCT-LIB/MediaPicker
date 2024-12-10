package com.mct.mediapicker.adapter;

import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Function;
import androidx.recyclerview.widget.RecyclerView;

import com.mct.mediapicker.MediaUtils;
import com.mct.mediapicker.R;
import com.mct.mediapicker.common.dragselect.DragSelectTouchListener;
import com.mct.mediapicker.common.dragselect.DragSelectionProcessor;
import com.mct.mediapicker.databinding.MpLayoutItemMediaBinding;
import com.mct.mediapicker.fragment.MediaLoaderDelegate;
import com.mct.mediapicker.model.Album;
import com.mct.mediapicker.model.Media;
import com.mct.touchutils.TouchUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> implements
        DragSelectionProcessor.ISelectionHandler,
        DragSelectionProcessor.ISelectionStartFinishedListener {

    private final Set<MediaViewHolder> boundViewHolders;

    private final boolean isMultipleSelect;
    private final List<Media> items;
    private final OnItemClickListener onItemClickListener;
    private final OnItemDragListener onItemDragListener;
    private final Function<Media, Boolean> evaluateMediaPick;

    private RecyclerView recyclerView;
    private DragSelectTouchListener dragSelectTouchListener;

    public MediaAdapter(boolean isMultipleSelect,
                        List<Album> albums,
                        OnItemClickListener onItemClickListener,
                        OnItemDragListener onItemDragListener,
                        Function<Media, Boolean> evaluateMediaPick) {
        this.boundViewHolders = new HashSet<>();
        this.isMultipleSelect = isMultipleSelect;
        this.items = processAlbums(albums);
        this.onItemClickListener = onItemClickListener;
        this.onItemDragListener = onItemDragListener;
        this.evaluateMediaPick = evaluateMediaPick;
    }

    @NonNull
    private List<Media> processAlbums(@NonNull List<Album> albums) {
        return albums.parallelStream()
                .map(Album::getMediaList)
                .flatMap(List::parallelStream)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(m -> -m.getDateModified()))
                .collect(Collectors.toList());
    }

    public void setDragSelectTouchListener(DragSelectTouchListener listener) {
        dragSelectTouchListener = listener;
    }

    public Media getMedia(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        } else {
            return items.get(position);
        }
    }

    public void invalidateSelect() {
        for (MediaViewHolder holder : boundViewHolders) {
            holder.setSelected(isMultipleSelect, isSelected(holder.getAdapterPosition()));
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new MediaViewHolder(MpLayoutItemMediaBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Media media = items.get(position);
        if (media == null) {
            return;
        }
        holder.load(media, isMultipleSelect, evaluateMediaPick.apply(media));
        holder.binding.mpIvScale.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onScaleClick(media, holder.getAdapterPosition());
            }
            performHapticFeedback(holder.itemView);
        });
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(media, holder.getAdapterPosition());
            }
            if (isMultipleSelect) {
                holder.setSelected(true, isSelected(holder.getAdapterPosition()));
                performHapticFeedback(holder.itemView);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (!isMultipleSelect) {
                return false;
            }
            if (dragSelectTouchListener != null) {
                dragSelectTouchListener.startDragSelection(holder.getAdapterPosition());
            }
            return false;
        });
        if (holder.isRecyclable()) {
            boundViewHolders.add(holder);
        }
    }

    @Override
    public void onViewRecycled(@NonNull MediaViewHolder holder) {
        super.onViewRecycled(holder);
        boundViewHolders.remove(holder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull MediaViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.delegate.onAttach(holder.itemView);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MediaViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.delegate.onDetach(holder.itemView);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    /* --- drag selection --- */

    private int startPosition = -1;

    @Override
    public boolean isSelected(int index) {
        return evaluateMediaPick.apply(getMedia(index));
    }

    @Override
    public void updateSelection(int start, int end, boolean isSelected, boolean calledFromOnStart) {
        if (onItemDragListener != null) {
            List<Media> media = new ArrayList<>();
            if (startPosition > end) {
                for (int i = end; i > start - 1; i--) {
                    media.add(getMedia(i));
                }
            } else {
                for (int i = start; i <= end; i++) {
                    media.add(getMedia(i));
                }
            }
            if (onItemDragListener.onDragSelectionChanged(media, isSelected)) {
                for (MediaViewHolder holder : boundViewHolders) {
                    int position = holder.getAdapterPosition();
                    if (position > start || position <= end) {
                        holder.setSelected(isMultipleSelect, isSelected(position));
                    }
                }
                performHapticFeedback(recyclerView);
            } else {
                if (calledFromOnStart) {
                    performHapticFeedback(recyclerView);
                }
            }
        }
    }

    @Override
    public void onSelectionStarted(int start, boolean originalSelectionState) {
        startPosition = start;
        if (onItemDragListener != null) {
            onItemDragListener.onDragSelectionStart(start);
        }
    }

    @Override
    public void onSelectionFinished(int end) {
        if (onItemDragListener != null) {
            onItemDragListener.onDragSelectionFinish(end);
        }
    }

    private static void performHapticFeedback(View view) {
        if (view != null) {
            view.performHapticFeedback(
                    HapticFeedbackConstants.LONG_PRESS,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            );
        }
    }

    public static class MediaViewHolder extends BindingViewHolder<MpLayoutItemMediaBinding> {

        private final MediaLoaderDelegate delegate;

        public MediaViewHolder(@NonNull MpLayoutItemMediaBinding binding) {
            super(binding);
            delegate = MediaLoaderDelegate.create(itemView.getContext());
            delegate.setListener(bitmap -> {
                binding.mpIvThumb.setImageBitmap(bitmap);
                if (bitmap != null) {
                    runAfterLoadTask(binding.mpTvDuration);
                    runAfterLoadTask(binding.mpIvSelected);
                    runAfterLoadTask(binding.mpIvScale);
                }
            });
            // @formatter:off
            TouchUtils.setTouchListener(itemView, new TouchUtils.TouchScaleListener(){
                protected float getPressScale() {return 0.05f;}
                protected float getReleaseScale() {return 0.01f;}
            });
            // @formatter:on
        }

        void load(Media media, boolean selectVisible, boolean isSelected) {
            // prepare task
            setAfterLoadTask(binding.mpTvDuration, () -> setDuration(media.isVideo(), media.getDuration()));
            setAfterLoadTask(binding.mpIvSelected, () -> setSelected(selectVisible, isSelected));
            setAfterLoadTask(binding.mpIvScale, this::setScaleVisible);

            // hide unnecessary views
            binding.mpTvDuration.setVisibility(View.GONE);
            binding.mpIvSelected.setVisibility(View.GONE);
            binding.mpIvScale.setVisibility(View.GONE);

            // load thumbnail
            delegate.loadThumbnail(media);
        }

        void setDuration(boolean video, Integer duration) {
            // clear task
            setAfterLoadTask(binding.mpTvDuration, null);

            if (video && duration != null) {
                if (duration == 0) {
                    binding.mpTvDuration.setText("");
                    binding.mpTvDuration.setCompoundDrawablePadding(0);
                } else {
                    int d = duration / 1000;
                    String txt = String.format(Locale.getDefault(), "%d:%02d", d / 60, d % 60);
                    binding.mpTvDuration.setText(txt);
                    binding.mpTvDuration.setCompoundDrawablePadding(MediaUtils.dp2px(2));
                }
                binding.mpTvDuration.setVisibility(View.VISIBLE);
            } else {
                binding.mpTvDuration.setVisibility(View.GONE);
            }
        }

        void setSelected(boolean visible, boolean selected) {
            // clear task
            setAfterLoadTask(binding.mpIvSelected, null);

            binding.mpIvSelected.setVisibility(visible ? View.VISIBLE : View.GONE);
            binding.mpIvSelected.setSelected(selected);
        }

        void setScaleVisible() {
            // clear task
            setAfterLoadTask(binding.mpIvScale, null);

            binding.mpIvScale.setVisibility(View.VISIBLE);
        }

        private void runAfterLoadTask(@NonNull View view) {
            Object object = view.getTag(R.id.mp_tag_task_after_load);
            if (object instanceof Runnable) {
                ((Runnable) object).run();
            }
        }

        private void setAfterLoadTask(@NonNull View view, Runnable runnable) {
            view.setTag(R.id.mp_tag_task_after_load, runnable);
        }

    }

    public interface OnItemClickListener {
        void onItemClick(Media media, int position);

        void onScaleClick(Media media, int position);
    }

    public interface OnItemDragListener {
        void onDragSelectionStart(int position);

        void onDragSelectionFinish(int position);

        boolean onDragSelectionChanged(List<Media> media, boolean isSelected);
    }
}
