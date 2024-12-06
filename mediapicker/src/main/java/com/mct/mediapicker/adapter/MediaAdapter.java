package com.mct.mediapicker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mct.mediapicker.databinding.MpLayoutItemMediaBinding;
import com.mct.mediapicker.model.Album;
import com.mct.mediapicker.model.Media;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    private final Set<ViewHolder> boundViewHolders;

    private final boolean isMultipleSelect;
    private final List<Media> medias;
    private final Function<Media, Boolean> evaluateMediaPick;
    private final OnItemClickListener listener;

    public MediaAdapter(boolean isMultipleSelect, @NonNull List<Album> albums, OnItemClickListener listener, Function<Media, Boolean> evaluateMediaPick) {
        List<Media> media = albums.stream().map(Album::getMediaList).flatMap(List::stream)
                .sorted(Comparator.comparingLong(m -> -m.getDateModified()))
                .collect(Collectors.toList());

        this.boundViewHolders = new HashSet<>();
        this.isMultipleSelect = isMultipleSelect;
        this.medias = media;
        this.listener = listener;
        this.evaluateMediaPick = evaluateMediaPick;
    }

    public void invalidateSelect() {
        for (ViewHolder holder : boundViewHolders) {
            Media media = medias.get(holder.getAdapterPosition());
            holder.setSelected(isMultipleSelect, evaluateMediaPick.apply(media));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(MpLayoutItemMediaBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Media media = medias.get(position);
        if (media == null) {
            return;
        }
        Glide.with(holder.itemView)
                .load(media.getUri())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.binding.mpIvThumb);
        holder.setSelected(isMultipleSelect, evaluateMediaPick.apply(media));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(media, position);
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
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        boundViewHolders.remove(holder);
    }

    @Override
    public int getItemCount() {
        return medias == null ? 0 : medias.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        MpLayoutItemMediaBinding binding;

        public ViewHolder(@NonNull MpLayoutItemMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
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
