package com.mct.mediapicker.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.recyclerview.widget.RecyclerView;

import com.mct.mediapicker.R;
import com.mct.mediapicker.adapter.MediaAdapter;
import com.mct.mediapicker.model.Album;

import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class MediaTabFragment extends BaseTabFragment {

    @Override
    protected String getEmptyMessage() {
        return getString(R.string.mp_empty_media);
    }

    @Override
    protected void displayData(@NonNull RecyclerView rcv, List<Album> albums) {
        if (presenter == null) {
            return;
        }
        PickerFragment f = getPhotoPickerFragment();
        SetupDataHelper.setup(rcv, presenter, albums, f, f);
    }

    public void invalidateSelectedMedia() {
        if (getContext() == null || binding == null) {
            return;
        }
        RecyclerView.Adapter<?> adapter = binding.mpRecyclerView.getAdapter();
        if (adapter instanceof MediaAdapter) {
            ((MediaAdapter) adapter).invalidateSelect();
        }
    }
}
