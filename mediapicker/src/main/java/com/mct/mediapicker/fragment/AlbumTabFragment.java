package com.mct.mediapicker.fragment;

import androidx.annotation.RestrictTo;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mct.mediapicker.R;
import com.mct.mediapicker.adapter.AlbumAdapter;
import com.mct.mediapicker.adapter.decoration.GridSpacingItemDecoration;
import com.mct.mediapicker.model.Album;

import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class AlbumTabFragment extends BaseTabFragment {

    @Override
    protected String getEmptyMessage() {
        return getString(R.string.mp_empty_album);
    }

    @Override
    protected RecyclerView.Adapter<?> onCreateAdapter(List<Album> albums) {
        return new AlbumAdapter(albums, (album, position) -> {
            if (getPhotoPickerFragment() != null) {
                getPhotoPickerFragment().showDetailAlbum(album);
            }
        });
    }

    @Override
    protected RecyclerView.LayoutManager onCreateLayoutManager() {
        return new GridLayoutManager(getContext(), 2);
    }

    @Override
    protected RecyclerView.ItemDecoration onCreateItemDecoration() {
        return new GridSpacingItemDecoration(2, Utils.dp2px(16), true, 0);
    }
}
