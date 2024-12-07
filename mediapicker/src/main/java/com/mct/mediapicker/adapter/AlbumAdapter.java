package com.mct.mediapicker.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mct.mediapicker.databinding.MpLayoutItemAlbumBinding;
import com.mct.mediapicker.model.Album;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private final List<Album> albums;
    private final OnClickItemListener listener;

    public AlbumAdapter(List<Album> albums, OnClickItemListener listener) {
        this.albums = albums;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new AlbumViewHolder(MpLayoutItemAlbumBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albums.get(position);
        if (album == null) {
            return;
        }
        Glide.with(holder.itemView)
                .load(album.getBucketThumbUri())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.binding.mpIvThumb);
        holder.binding.mpTvTitle.setText(album.getBucketName());
        holder.binding.mpTvDesc.setText(String.valueOf(album.getMediaList().size()));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClickItem(album, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return albums == null ? 0 : albums.size();
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {

        MpLayoutItemAlbumBinding binding;

        public AlbumViewHolder(@NonNull MpLayoutItemAlbumBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnClickItemListener {
        void onClickItem(Album album, int position);
    }
}
