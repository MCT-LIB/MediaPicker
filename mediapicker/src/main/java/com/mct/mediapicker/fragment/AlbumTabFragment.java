package com.mct.mediapicker.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mct.mediapicker.MediaUtils;
import com.mct.mediapicker.R;
import com.mct.mediapicker.adapter.AlbumAdapter;
import com.mct.mediapicker.model.Album;

import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class AlbumTabFragment extends BaseTabFragment {

    @Override
    protected String getEmptyMessage() {
        return getString(R.string.mp_empty_album);
    }

    @Override
    protected void displayData(@NonNull RecyclerView rcv, List<Album> albums) {
        for (int i = 0; i < rcv.getItemDecorationCount(); i++) {
            if (rcv.getItemDecorationAt(i) instanceof SpacingGridItemDecoration) {
                rcv.removeItemDecorationAt(i);
            }
        }
        rcv.addItemDecoration(new SpacingGridItemDecoration(2, MediaUtils.dp2px(16), true, 0));
        rcv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rcv.setAdapter(new AlbumAdapter(albums, (album, position) -> {
            if (getPhotoPickerFragment() != null) {
                getPhotoPickerFragment().showDetailAlbum(album);
            }
        }));
    }

}
