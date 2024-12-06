package com.mct.mediapicker.fragment;

import androidx.annotation.RestrictTo;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mct.mediapicker.R;
import com.mct.mediapicker.adapter.MediaAdapter;
import com.mct.mediapicker.adapter.decoration.GridSpacingItemDecoration;
import com.mct.mediapicker.model.Album;

import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class MediaTabFragment extends BaseTabFragment {

    @Override
    protected String getEmptyMessage() {
        return getString(R.string.mp_empty_media);
    }

    @Override
    protected RecyclerView.Adapter<?> onCreateAdapter(List<Album> albums) {
        PhotoPickerFragment picker = getPhotoPickerFragment();
        return new MediaAdapter(picker.getPresenter().isMultipleSelect(), albums, picker, picker.getPresenter()::isSelectedMedia);
    }

    @Override
    protected RecyclerView.LayoutManager onCreateLayoutManager() {
        return new GridLayoutManager(getContext(), 3);
    }

    @Override
    protected RecyclerView.ItemDecoration onCreateItemDecoration() {
        return new GridSpacingItemDecoration(3, Utils.dp2px(3), false, 0);
    }

    public void invalidateSelectedMedia() {
        RecyclerView.Adapter<?> adapter = binding.mpRecyclerView.getAdapter();
        if (adapter instanceof MediaAdapter) {
            ((MediaAdapter) adapter).invalidateSelect();
        }
    }
}
