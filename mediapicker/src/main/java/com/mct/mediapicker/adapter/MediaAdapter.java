package com.mct.mediapicker.adapter;

import static com.bumptech.glide.load.engine.DiskCacheStrategy.AUTOMATIC;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.NONE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.mct.mediapicker.MediaUtils;
import com.mct.mediapicker.databinding.MpLayoutItemMediaBinding;
import com.mct.mediapicker.model.Album;
import com.mct.mediapicker.model.Media;

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
    private final List<Media> medias;
    private final OnItemClickListener listener;
    private final Function<Media, Boolean> evaluateMediaPick;

    private int thumbnailSize;
    private boolean dragging = false;

    public MediaAdapter(boolean isMultipleSelect, @NonNull List<Album> albums, OnItemClickListener listener, Function<Media, Boolean> evaluateMediaPick) {
        List<Media> media = albums.parallelStream()
                .map(Album::getMediaList)
                .flatMap(List::parallelStream)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(m -> -m.getDateModified()))
                .collect(Collectors.toList());

        this.boundViewHolders = new HashSet<>();
        this.isMultipleSelect = isMultipleSelect;
        this.medias = media;
        this.listener = listener;
        this.evaluateMediaPick = evaluateMediaPick;
    }

    public Media getMedias(int position) {
        if (position < 0 || position >= medias.size()) {
            return null;
        } else {
            return medias.get(position);
        }
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
        if (!dragging) {
            for (MediaViewHolder holder : boundViewHolders) {
                holder.loadImage(medias.get(holder.getAdapterPosition()), thumbnailSize, false);
            }
        }
    }

    public void invalidateSelect() {
        for (MediaViewHolder holder : boundViewHolders) {
            Media media = medias.get(holder.getAdapterPosition());
            holder.setSelected(isMultipleSelect, evaluateMediaPick.apply(media));
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        thumbnailSize = MediaUtils.getScreenWidth(recyclerView.getContext()) / 3;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new MediaViewHolder(MpLayoutItemMediaBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Media media = medias.get(position);
        holder.loadImage(media, thumbnailSize, dragging);
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
    public int getItemCount() {
        return medias == null ? 0 : medias.size();
    }

    public static class MediaViewHolder extends RecyclerView.ViewHolder {

        MpLayoutItemMediaBinding binding;

        public MediaViewHolder(@NonNull MpLayoutItemMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void loadImage(Media media, int size, boolean dragging) {
            if (dragging) {
                binding.mpIvThumb.setImageDrawable(null);
            } else {
                Glide.with(itemView)
                        .load(media.getUri())
                        .signature(new ObjectKey(media.getDateModified()))
                        .diskCacheStrategy(media.isVideo() ? AUTOMATIC : NONE)
                        .override(size)
                        .into(binding.mpIvThumb);
            }
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
