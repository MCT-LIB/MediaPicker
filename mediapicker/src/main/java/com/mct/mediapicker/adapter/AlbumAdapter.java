package com.mct.mediapicker.adapter;

import static com.bumptech.glide.load.engine.DiskCacheStrategy.AUTOMATIC;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.NONE;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.mct.mediapicker.databinding.MpLayoutItemAlbumBinding;
import com.mct.mediapicker.model.Album;
import com.mct.mediapicker.model.Media;

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
        Media media = album.getLastMedia();
        holder.loadImage(media != null ? media : new Media());
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

    public static class AlbumViewHolder extends BindingViewHolder<MpLayoutItemAlbumBinding> {

        public AlbumViewHolder(@NonNull MpLayoutItemAlbumBinding binding) {
            super(binding);
        }

        void loadImage(@NonNull Media media) {
            ImageView ivThumb = binding.mpIvThumb;
            if (ivThumb.getHandler() == null) {
                ivThumb.post(() -> loadImage(media));
                return;
            }
            Glide.with(ivThumb)
                    .load(media.getUri())
                    .signature(new ObjectKey(media.getDateModified()))
                    .diskCacheStrategy(media.isVideo() ? AUTOMATIC : NONE)
                    .override(ivThumb.getWidth())
                    .into(ivThumb);
        }
    }

    public interface OnClickItemListener {
        void onClickItem(Album album, int position);
    }
}
