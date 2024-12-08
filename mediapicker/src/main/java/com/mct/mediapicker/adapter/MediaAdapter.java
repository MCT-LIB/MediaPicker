package com.mct.mediapicker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Function;
import androidx.recyclerview.widget.RecyclerView;

import com.mct.mediapicker.databinding.MpLayoutItemMediaBinding;
import com.mct.mediapicker.fragment.MediaLoaderDelegate;
import com.mct.mediapicker.model.Album;
import com.mct.mediapicker.model.Media;
import com.mct.touchutils.TouchUtils;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private final Set<MediaViewHolder> boundViewHolders;

    private final boolean isMultipleSelect;
    private final List<Media> items;
    private final OnItemClickListener listener;
    private final Function<Media, Boolean> evaluateMediaPick;

    public MediaAdapter(boolean isMultipleSelect, @NonNull List<Album> albums, OnItemClickListener listener, Function<Media, Boolean> evaluateMediaPick) {
        this.boundViewHolders = new HashSet<>();
        this.isMultipleSelect = isMultipleSelect;
        this.items = processAlbums(albums);
        this.listener = listener;
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

    public Media getMedia(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        } else {
            return items.get(position);
        }
    }

    public void invalidateSelect() {
        for (MediaViewHolder holder : boundViewHolders) {
            Media media = items.get(holder.getAdapterPosition());
            holder.setSelected(isMultipleSelect, evaluateMediaPick.apply(media));
        }
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
        holder.loadImage(media);
        holder.setDuration(media.isVideo(), media.getDuration());
        holder.setSelected(isMultipleSelect, evaluateMediaPick.apply(media));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(media, holder.getAdapterPosition());
            }
            if (isMultipleSelect) {
                holder.setSelected(true, evaluateMediaPick.apply(media));
            }
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

    public static class MediaViewHolder extends BindingViewHolder<MpLayoutItemMediaBinding> {

        private final MediaLoaderDelegate delegate;

        public MediaViewHolder(@NonNull MpLayoutItemMediaBinding binding) {
            super(binding);
            // @formatter:off
            TouchUtils.setTouchListener(itemView, new TouchUtils.TouchScaleListener(){
                protected float getPressScale() {return 0.075f;}
                protected float getReleaseScale() {return 0.025f;}
            });
            // @formatter:on
            delegate = MediaLoaderDelegate.create(itemView.getContext());
            delegate.setListener(binding.mpIvThumb::setImageBitmap);
        }

        void loadImage(Media media) {
            delegate.loadThumbnail(media);
        }

        void setDuration(boolean video, Integer duration) {
            if (video && duration != null) {
                duration /= 1000;
                String txt = String.format(Locale.getDefault(), "%d:%02d", duration / 60, duration % 60);
                binding.mpTvDuration.setText(txt);
                binding.mpTvDuration.setVisibility(View.VISIBLE);
            } else {
                binding.mpTvDuration.setVisibility(View.GONE);
            }
        }

        void setSelected(boolean visible, boolean selected) {
            binding.mpIvSelected.setVisibility(visible ? View.VISIBLE : View.GONE);
            binding.mpIvSelected.setSelected(selected);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Media media, int position);
    }
}
