package com.mct.mediapicker.fragment;

import static com.mct.mediapicker.MediaPickerOption.PICK_TYPE_ALL;
import static com.mct.mediapicker.MediaPickerOption.PICK_TYPE_IMAGE;
import static com.mct.mediapicker.MediaPickerOption.PICK_TYPE_VIDEO;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mct.mediapicker.MediaPickerOption;
import com.mct.mediapicker.MediaUtils;
import com.mct.mediapicker.R;
import com.mct.mediapicker.adapter.MediaAdapter;
import com.mct.mediapicker.databinding.MpBtsBinding;
import com.mct.mediapicker.model.Album;
import com.mct.mediapicker.model.Media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class PickerFragment extends BottomSheetDialogFragment implements MediaAdapter.OnItemClickListener {

    @NonNull
    public static PickerFragment newInstance(@NonNull MediaPickerOption option) {
        PickerFragment fragment = new PickerFragment();
        fragment.option = option;
        return fragment;
    }

    private MpBtsBinding binding;
    private Presenter presenter;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    private Album album;
    private MediaPickerOption option;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        presenter = new Presenter();
        presenter.attach(() -> option);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        presenter.detach();
        presenter = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("album", album);
        outState.putString("optionId", option.getId());
        outState.putSerializable("selectedMedia", new ArrayList<>(presenter.getSelectedMedia()));
        OptionHolder.saveOption(option);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(@Nullable Bundle ss) {
        super.onCreate(ss);
        if (ss != null) {
            album = (Album) ss.getSerializable("album");
            option = OptionHolder.restoredOption(ss.getString("optionId"));
            presenter.setSelectedMedia((ArrayList<Media>) ss.getSerializable("selectedMedia"));
        }
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (isPermissionGranted()) {
                findTabFragment(0, BaseTabFragment.class).ifPresent(BaseTabFragment::loadData);
                findTabFragment(1, BaseTabFragment.class).ifPresent(BaseTabFragment::loadData);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        album = null;
        option = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        if (!MediaUtils.isMaterial3Theme(context)) {
            context = new ContextThemeWrapper(context, R.style.PhotoPickerTheme);
        }
        BottomSheetDialog dialog = new BottomSheetDialog(context, getTheme());
        if (presenter.isMultipleSelect()) {
            BottomSheetBehavior<?> behavior = dialog.getBehavior();
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = MpBtsBinding.inflate(inflater)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // add back press callback
        ((BottomSheetDialog) requireDialog())
                .getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (album != null) {
                            dismissDetailAlbum();
                        } else {
                            dismiss();
                        }
                    }
                });

        binding.mpToolbar.setNavigationOnClickListener(v -> dismiss());
        binding.mpViewpager.setAdapter(new FragmentTabAdapter(getChildFragmentManager()));
        binding.mpTabLayout.setupWithViewPager(binding.mpViewpager);
        binding.mpViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < binding.mpViewpager.getChildCount(); i++) {
                    View v = binding.mpViewpager.getChildAt(i).findViewById(R.id.mp_recycler_view);
                    if (v != null) {
                        v.setNestedScrollingEnabled(i == position);
                    }
                }
            }
        });

        // show selected media if exist
        if (presenter.isMultipleSelect()) {
            invalidateSelectedMedia();
        }

        // show detail album if exist
        if (album != null) {
            showDetailAlbum(album);
        } else {
            dismissDetailAlbum();
        }

        // request permission
        if (!isPermissionGranted()) {
            requestPermissions();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (isStateSaved()) {
            return;
        }
        presenter.setSelectedMedia(null);
        presenter.submitSelectedMedia();
    }

    @Override
    public void onItemClick(Media media, int position) {
        if (media == null) {
            return;
        }
        if (presenter.isSelectedMedia(media)) {
            presenter.removeSelectedMedia(media);
        } else {
            if (presenter.getSelectedMediaCount() < presenter.getMaxSelection()) {
                presenter.addSelectedMedia(media);
            }
        }
        if (presenter.isMultipleSelect()) {
            invalidateSelectedMedia();
        } else {
            presenter.submitSelectedMedia();
            dismiss();
        }
    }

    private void invalidateSelectedMedia() {
        int getSelectedMediaCount = presenter.getSelectedMediaCount();
        View bottomBar = binding.mpBottomBar;
        if (getSelectedMediaCount > 0) {
            bottomBar.animate()
                    .withStartAction(() -> bottomBar.setVisibility(View.VISIBLE))
                    .withEndAction(null)
                    .setInterpolator(new AccelerateInterpolator())
                    .setDuration(200)
                    .translationY(0)
                    .start();
        } else {
            bottomBar.animate()
                    .withStartAction(null)
                    .withEndAction(() -> bottomBar.setVisibility(View.GONE))
                    .setInterpolator(new DecelerateInterpolator())
                    .setDuration(200)
                    .translationY(bottomBar.getHeight())
                    .start();
        }
        binding.mpBtnAdd.setEnabled(getSelectedMediaCount > 0);
        binding.mpBtnAdd.setText(getString(R.string.mp_add_n, presenter.getSelectedMediaCount()));
        binding.mpBtnAdd.setOnClickListener(v -> {
            presenter.submitSelectedMedia();
            dismiss();
        });
    }

    void showDetailAlbum(@NonNull Album a) {
        album = a;
        binding.mpAlbumDetail.getRoot().setVisibility(View.VISIBLE);
        binding.mpAlbumDetail.mpRecyclerView.setVisibility(View.VISIBLE);
        binding.mpViewpager.setVisibility(View.GONE);
        binding.mpTabLayout.setVisibility(View.GONE);
        binding.mpToolbar.setTitle(album.getBucketName());
        binding.mpToolbar.setNavigationIcon(R.drawable.mp_ic_back);
        binding.mpToolbar.setNavigationOnClickListener(v -> dismissDetailAlbum());

        RecyclerView rcv = binding.mpAlbumDetail.mpRecyclerView;
        for (int i = 0; i < rcv.getItemDecorationCount(); i++) {
            if (rcv.getItemDecorationAt(i) instanceof SpacingGridItemDecoration) {
                rcv.removeItemDecorationAt(i);
            }
        }
        rcv.setAdapter(new MediaAdapter(presenter.isMultipleSelect(), Collections.singletonList(album), this, presenter::isSelectedMedia));
        rcv.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rcv.addItemDecoration(new SpacingGridItemDecoration(3, MediaUtils.dp2px(3), false, 0));
        ScrollHelper.attachTo(rcv);
    }

    void dismissDetailAlbum() {
        album = null;
        binding.mpAlbumDetail.getRoot().setVisibility(View.GONE);
        binding.mpAlbumDetail.mpRecyclerView.setVisibility(View.GONE);
        binding.mpAlbumDetail.mpRecyclerView.setAdapter(null);
        binding.mpViewpager.setVisibility(View.VISIBLE);
        binding.mpTabLayout.setVisibility(View.VISIBLE);
        binding.mpToolbar.setTitle(null);
        binding.mpToolbar.setNavigationIcon(R.drawable.mp_ic_close);
        binding.mpToolbar.setNavigationOnClickListener(v -> dismiss());

        findTabFragment(0, MediaTabFragment.class).ifPresent(MediaTabFragment::invalidateSelectedMedia);
    }

    private void requestPermissions() {
        String[] permissions;
        int pickType = presenter.getOption().getPickType();
        switch (pickType) {
            case PICK_TYPE_IMAGE:
                permissions = MediaUtils.getImagesPermissions();
                break;
            case PICK_TYPE_VIDEO:
                permissions = MediaUtils.getVideosPermissions();
                break;
            case PICK_TYPE_ALL:
                permissions = MediaUtils.getMediaPermissions();
                break;
            default:
                throw new IllegalArgumentException("Unknown pick type: " + pickType);
        }
        requestPermissionLauncher.launch(permissions);
    }

    private boolean isPermissionGranted() {
        int pickType = presenter.getOption().getPickType();
        switch (pickType) {
            case PICK_TYPE_IMAGE:
                return MediaUtils.canLoadImages(requireContext());
            case PICK_TYPE_VIDEO:
                return MediaUtils.canLoadVideos(requireContext());
            case PICK_TYPE_ALL:
                return MediaUtils.canLoadMedia(requireContext());
            default:
                throw new IllegalArgumentException("Unknown pick type: " + pickType);
        }
    }

    Presenter getPresenter() {
        return presenter;
    }

    private <F extends Fragment> Optional<F> findTabFragment(int index, @NonNull Class<F> clazz) {
        return Optional.ofNullable(findTabFragment(index)).filter(clazz::isInstance).map(clazz::cast);
    }

    private Fragment findTabFragment(int index) {
        return getChildFragmentManager().findFragmentByTag("android:switcher:" + binding.mpViewpager.getId() + ":" + index);
    }

    @SuppressWarnings("deprecation")
    class FragmentTabAdapter extends FragmentPagerAdapter {

        public FragmentTabAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return position == 0 ? new MediaTabFragment() : new AlbumTabFragment();
        }

        @Override
        public int getCount() {
            return 2;
        }

        @NonNull
        @Override
        public CharSequence getPageTitle(int position) {
            return position == 0
                    ? getString(R.string.mp_media)
                    : getString(R.string.mp_album);
        }

    }

    private static class OptionHolder {

        private static final Map<String, MediaPickerOption> options = new ArrayMap<>();

        private static void saveOption(@NonNull MediaPickerOption option) {
            options.put(option.getId(), option);
        }

        private static MediaPickerOption restoredOption(String optionId) {
            return options.remove(optionId);
        }
    }

}
