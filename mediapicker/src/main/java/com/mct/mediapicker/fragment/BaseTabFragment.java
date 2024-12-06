package com.mct.mediapicker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.mct.mediapicker.R;
import com.mct.mediapicker.databinding.MpLayoutDataBinding;
import com.mct.mediapicker.model.Album;

import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class BaseTabFragment extends Fragment {

    protected MpLayoutDataBinding binding;
    private Presenter presenter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(getParentFragment() instanceof PhotoPickerFragment)) {
            throw new IllegalArgumentException("Parent fragment must be PhotoPickerFragment");
        }
        presenter = getPhotoPickerFragment().getPresenter();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        presenter = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = MpLayoutDataBinding.inflate(inflater)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @NonNull
    @Override
    public LayoutInflater onGetLayoutInflater(@Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        if (!Utils.isMaterial3Theme(context)) {
            context = new ContextThemeWrapper(context, R.style.PhotoPickerTheme);
        }
        return super.onGetLayoutInflater(savedInstanceState).cloneInContext(context);
    }

    public void loadData() {
        if (getContext() == null || presenter == null) {
            return;
        }
        show(binding.mpProgressIndicator);
        presenter.getAlbums(getContext(), albums -> {
            if (albums == null || albums.isEmpty()) {
                show(binding.mpTvEmptyMessage);
                binding.mpTvEmptyMessage.setText(getEmptyMessage());
            } else {
                show(binding.mpRecyclerView);
                RecyclerView rcv = binding.mpRecyclerView;
                for (int i = 0; i < rcv.getItemDecorationCount(); i++) {
                    rcv.removeItemDecorationAt(i);
                }
                rcv.setAdapter(onCreateAdapter(albums));
                rcv.setLayoutManager(onCreateLayoutManager());
                rcv.addItemDecoration(onCreateItemDecoration());
            }
        });
    }

    protected PhotoPickerFragment getPhotoPickerFragment() {
        return (PhotoPickerFragment) getParentFragment();
    }

    protected abstract String getEmptyMessage();

    protected abstract RecyclerView.Adapter<?> onCreateAdapter(List<Album> albums);

    protected abstract RecyclerView.LayoutManager onCreateLayoutManager();

    protected abstract RecyclerView.ItemDecoration onCreateItemDecoration();

    private void show(@NonNull View show) {
        View[] views = new View[]{
                binding.mpProgressIndicator,
                binding.mpTvEmptyMessage,
                binding.mpRecyclerView
        };
        for (View view : views) {
            view.setVisibility(show == view ? View.VISIBLE : View.GONE);
        }
    }
}
