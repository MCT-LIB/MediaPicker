package com.mct.mediapicker.fragment;

import static androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mct.mediapicker.MediaPickerOption;
import com.mct.mediapicker.MediaUtils;
import com.mct.mediapicker.R;
import com.mct.mediapicker.adapter.MediaAdapter;
import com.mct.mediapicker.databinding.MpFragmentBtsPickerBinding;
import com.mct.mediapicker.model.Album;
import com.mct.mediapicker.model.Media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

// TODO: 21/04/2025 Translate string res
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class PickerFragment extends BottomSheetDialogFragment implements MediaAdapter.OnItemClickListener, MediaAdapter.OnItemDragListener {

    private static final int ANIM_DURATION = 200;

    @NonNull
    public static PickerFragment newInstance(@NonNull MediaPickerOption option) {
        PickerFragment fragment = new PickerFragment();
        fragment.option = option;
        return fragment;
    }

    private MpFragmentBtsPickerBinding binding;
    private Presenter presenter;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    private Media media;
    private Album album;
    private MediaPickerOption option;
    private MediaLoaderDelegate delegate;

    private boolean pendingLoadData;

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
        Presenter.saveOption(option);
        outState.putParcelable("media", media);
        outState.putParcelable("album", album);
        outState.putString("optionId", option.getId());
        outState.putParcelableArrayList("selectedMedia", new ArrayList<>(presenter.getSelectedMedia()));
    }

    @Override
    public void onCreate(@Nullable Bundle ss) {
        super.onCreate(ss);
        if (ss != null) {
            media = ss.getParcelable("media");
            album = ss.getParcelable("album");
            option = Presenter.restoredOption(ss.getString("optionId"));
            presenter.setSelectedMedia(ss.getParcelableArrayList("selectedMedia"));
        }
        requestPermissionLauncher = registerForActivityResult(new RequestMultiplePermissions(), result -> loadData());
    }

    private void loadData() {
        // check valid state
        if (getContext() == null || presenter == null) {
            return;
        }
        // clear album
        presenter.resetAlbums();

        // load data
        if (isPermissionsGranted()) {
            findTabFragment(0, BaseTabFragment.class).ifPresent(BaseTabFragment::loadData);
            findTabFragment(1, BaseTabFragment.class).ifPresent(BaseTabFragment::loadData);
        }

        // init reselection view
        initReselection();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pendingLoadData) {
            loadData();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        media = null;
        album = null;
        option = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = presenter.getOption().getThemeStrategy().wrapContext(requireContext());
        return new BottomSheetDialog(context, getTheme());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = MpFragmentBtsPickerBinding.inflate(inflater)).getRoot();
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
                        onBackPressed();
                    }
                });

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
            setBottomSheetSkipCollapsed();
            setBottomSheetExpanded();
            view.post(this::invalidateSelectedMedia);
        }

        // show detail album if exist
        if (album != null) {
            showDetailAlbum(album);
        } else {
            view.post(this::dismissDetailAlbum);
        }

        // show media preview if exist
        if (media != null) {
            showMediaPreview(media);
        } else {
            view.post(this::dismissMediaPreview);
        }

        // check permission
        if (!isPermissionsGranted()) {
            requestPermission();
        }

        // load data
        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // clear media if exist
        binding.mpMediaPreview.mpVvMedia.stopPlayback();
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

    public void onBackPressed() {
        if (media != null) {
            dismissMediaPreview();
            invalidateSelectedMediaTab();
            return;
        }
        if (album != null) {
            dismissDetailAlbum();
            invalidateSelectedMediaTab();
            return;
        }
        if (presenter.getSelectedMediaCount() > 0) {
            presenter.setSelectedMedia(null);
            invalidateToolbar();
            invalidateSelectedMedia();
            invalidateSelectedMediaTab();
            return;
        }
        dismissAllowingStateLoss();
    }

    @Override
    public void onItemClick(Media media, int position) {
        if (media == null) {
            return;
        }
        if (presenter.isMultipleSelect()) {
            if (presenter.isSelectedMedia(media)) {
                presenter.removeSelectedMedia(media);
            } else {
                presenter.addSelectedMedia(media);
            }
            invalidateToolbar();
            invalidateSelectedMedia();
        } else {
            presenter.setSelectedMedia(Collections.singletonList(media));
            presenter.submitSelectedMedia();
            dismissAllowingStateLoss();
        }
    }

    @Override
    public void onScaleClick(Media media, int position) {
        if (media == null) {
            return;
        }
        showMediaPreview(media);
    }

    @Override
    public void onDragSelectionStart(int position) {
        setBottomSheetDraggable(false);
    }

    @Override
    public void onDragSelectionFinish(int position) {
        setBottomSheetDraggable(true);
    }

    @Override
    public boolean onDragSelectionChanged(List<Media> media, boolean isSelected) {
        boolean handled;
        if (isSelected) {
            handled = presenter.addSelectedMedia(media);
        } else {
            handled = presenter.removeSelectedMedia(media);
        }
        invalidateToolbar();
        invalidateSelectedMedia();
        return handled;
    }

    private void invalidateToolbar() {
        if (album != null) {
            binding.mpToolbar.setTitle(album.getBucketName());
            binding.mpToolbar.setNavigationIcon(R.drawable.mp_ic_back);
            binding.mpToolbar.setNavigationOnClickListener(v -> onBackPressed());
            return;
        }
        if (presenter.getSelectedMediaCount() > 0) {
            binding.mpToolbar.setTitle("");
            binding.mpToolbar.setNavigationIcon(R.drawable.mp_ic_back);
            binding.mpToolbar.setNavigationOnClickListener(v -> onBackPressed());
            return;
        }
        binding.mpToolbar.setTitle("");
        binding.mpToolbar.setNavigationIcon(R.drawable.mp_ic_close);
        binding.mpToolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void invalidateSelectedMedia() {
        int selectedCount = presenter.getSelectedMediaCount();
        int minSelection = presenter.getOption().getMinSelection();
        int maxSelection = presenter.getOption().getMaxSelection();
        boolean exactMode = minSelection == maxSelection;
        String count;
        String minTxt = minSelection == Integer.MAX_VALUE ? "∞" : String.valueOf(minSelection);
        String maxTxt = maxSelection == Integer.MAX_VALUE ? "∞" : String.valueOf(maxSelection);

        if (minSelection == 1 && maxSelection == Integer.MAX_VALUE) {
            count = String.format(Locale.getDefault(), "(%d)", selectedCount);
        } else if (exactMode) {
            count = String.format(Locale.getDefault(), "(%d/%s)", selectedCount, minTxt);
        } else {
            count = String.format(Locale.getDefault(), "(%d) | [%s-%s]", selectedCount, minTxt, maxTxt);
        }

        binding.mpTvSelectedItems.setText(getString(R.string.mp_selected, count));
        binding.mpBtnContinue.setEnabled(selectedCount >= minSelection && selectedCount <= maxSelection);
        binding.mpBtnContinue.setOnClickListener(v -> {
            presenter.submitSelectedMedia();
            dismissAllowingStateLoss();
        });

        if (selectedCount > 0 || exactMode) {
            showFromBottom(binding.mpBottomBar);
            updateRecyclerViewsPaddingBottom(MediaUtils.dp2px(72));
        } else {
            hideToBottom(binding.mpBottomBar);
            updateRecyclerViewsPaddingBottom(0);
        }
    }

    private void invalidateSelectedMediaTab() {
        findTabFragment(0, MediaTabFragment.class).ifPresent(MediaTabFragment::invalidateSelectedMedia);
    }

    private void updateRecyclerViewsPaddingBottom(int paddingBottom) {
        List<RecyclerView> recyclerViews = new ArrayList<>();

        recyclerViews.add(binding.mpAlbumDetail.mpRecyclerView);

        for (int i = 0; i < binding.mpViewpager.getChildCount(); i++) {
            View v = binding.mpViewpager.getChildAt(i).findViewById(R.id.mp_recycler_view);
            if (v instanceof RecyclerView) {
                recyclerViews.add((RecyclerView) v);
            }
        }

        for (RecyclerView recyclerView : recyclerViews) {
            recyclerView.setPadding(
                    recyclerView.getPaddingLeft(),
                    recyclerView.getPaddingTop(),
                    recyclerView.getPaddingRight(),
                    paddingBottom
            );
        }
    }

    Presenter getPresenter() {
        return presenter;
    }

    void showDetailAlbum(@NonNull Album a) {
        album = a;
        binding.mpAlbumDetail.getRoot().setVisibility(View.VISIBLE);
        binding.mpAlbumDetail.mpRecyclerView.setVisibility(View.VISIBLE);
        binding.mpViewpager.setVisibility(View.GONE);
        binding.mpTabLayout.setVisibility(View.GONE);
        invalidateToolbar();

        RecyclerView rcv = binding.mpAlbumDetail.mpRecyclerView;
        SetupDataHelper.setup(rcv, presenter, Collections.singletonList(album), this, this);
    }

    void dismissDetailAlbum() {
        album = null;
        binding.mpAlbumDetail.getRoot().setVisibility(View.GONE);
        binding.mpAlbumDetail.mpRecyclerView.setVisibility(View.GONE);
        binding.mpAlbumDetail.mpRecyclerView.setAdapter(null);
        binding.mpViewpager.setVisibility(View.VISIBLE);
        binding.mpTabLayout.setVisibility(View.VISIBLE);
        invalidateToolbar();
    }

    void showMediaPreview(@NonNull Media m) {
        media = m;
        setBottomSheetExpanded();
        setBottomSheetDraggable(false);
        setStatusBarColor(Color.BLACK, ANIM_DURATION);
        showFromBottom(binding.mpMediaPreview.getRoot());

        // set toolbar listener
        binding.mpMediaPreview.mpToolbar.setNavigationOnClickListener(v -> onBackPressed());

        Button btnSelect = binding.mpMediaPreview.mpBtnSelect;
        btnSelect.setText(presenter.isSelectedMedia(media) ? R.string.mp_unselect : R.string.mp_select);
        btnSelect.setOnClickListener(v -> {
            onItemClick(media, -1);
            if (presenter.isMultipleSelect()) {
                btnSelect.setText(presenter.isSelectedMedia(media) ? R.string.mp_unselect : R.string.mp_select);
            }
        });

        if (media.isImage()) {
            initImagePreview(media);
        } else {
            initVideoPreview(media);
        }
    }

    void dismissMediaPreview() {
        media = null;
        setBottomSheetDraggable(true);
        setStatusBarColor(Color.TRANSPARENT, 0);
        hideToBottom(binding.mpMediaPreview.getRoot());

        binding.mpMediaPreview.mpIvPlay.setVisibility(View.GONE);
        binding.mpMediaPreview.mpIvSound.setVisibility(View.GONE);
        binding.mpMediaPreview.mpVvMedia.setVisibility(View.GONE);
        binding.mpMediaPreview.mpVvMedia.stopPlayback();

        binding.mpMediaPreview.mpIvMedia.setVisibility(View.GONE);
        if (delegate != null) {
            delegate.onDetach(binding.mpMediaPreview.mpIvMedia);
            delegate = null;
        }
    }

    boolean isPermissionsGranted() {
        return MediaUtils.isPermissionsGranted(requireContext(), option.getPickType());
    }

    void requestPermission() {
        if (getActivity() == null) {
            return;
        }
        requestPermissionLauncher.launch(MediaUtils.getRequestPermissions(option.getPickType()));
    }

    void requestPermission2() {
        if (getActivity() == null) {
            return;
        }
        if (MediaUtils.shouldGoToSettings(getActivity(), option.getPickType())) {
            pendingLoadData = true;
            MediaUtils.gotoSettings(getActivity());
            return;
        }
        requestPermissionLauncher.launch(MediaUtils.getRequestPermissions(option.getPickType()));
    }

    private void initReselection() {
        if (MediaUtils.shouldReselection(requireContext(), option.getPickType())) {
            binding.mpFrameReselection.setVisibility(View.VISIBLE);
            binding.mpBtnManage.setOnClickListener(v -> requestPermission());
        } else {
            binding.mpFrameReselection.setVisibility(View.GONE);
            binding.mpBtnManage.setOnClickListener(null);
        }
    }

    private void initImagePreview(@NonNull Media media) {
        ImageView imageView = binding.mpMediaPreview.mpIvMedia;
        imageView.setVisibility(View.VISIBLE);
        if (delegate == null) {
            delegate = MediaLoaderDelegate.create(requireContext(), true);
            delegate.setListener(imageView::setImageBitmap);
        }
        delegate.loadThumbnail(media);
        delegate.onAttach(imageView);
    }

    private void initVideoPreview(@NonNull Media media) {
        VideoView videoView = binding.mpMediaPreview.mpVvMedia;
        ImageView ivPlay = binding.mpMediaPreview.mpIvPlay;
        ImageView ivSound = binding.mpMediaPreview.mpIvSound;

        Consumer<Boolean> showControl = (visible) -> {
            ivPlay.setVisibility(visible ? View.VISIBLE : View.GONE);
            ivSound.setVisibility(visible ? View.VISIBLE : View.GONE);
        };

        // Initialize VideoView
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoURI(media.getUri());

        // Toggle controls on click
        videoView.setOnClickListener(v -> showControl.accept(ivPlay.getVisibility() != View.VISIBLE));

        // Handle video completion
        videoView.setOnCompletionListener(mp -> {
            videoView.seekTo(1);
            ivPlay.setSelected(false);
            showControl.accept(true);
        });

        // Prepare video and handle playback
        videoView.setOnPreparedListener(mp -> {
            videoView.start();
            ivPlay.setSelected(true);
            showControl.accept(true);

            ivSound.setOnClickListener(v -> {
                boolean isSound = !ivSound.isSelected();
                ivSound.setSelected(isSound);
                mp.setVolume(isSound ? 1 : 0, isSound ? 1 : 0);
            });
            // Default to mute
            ivSound.setSelected(true);
            ivSound.performClick();
        });

        // Play/pause functionality
        ivPlay.setOnClickListener(v -> {
            if (videoView.isPlaying()) {
                videoView.pause();
                ivPlay.setSelected(false);
            } else {
                videoView.start();
                ivPlay.setSelected(true);
            }
        });
    }

    private void setStatusBarColor(int color, int delayMillis) {
        Runnable runnable = () -> Optional.ofNullable(getDialog()).map(Dialog::getWindow).ifPresent(w -> w.setStatusBarColor(color));
        MediaUtils.runOnUiThreadDelayed(runnable, delayMillis);
    }

    private void setBottomSheetDraggable(boolean draggable) {
        getBottomSheetBehavior().ifPresent(behavior -> behavior.setDraggable(draggable));
    }

    private void setBottomSheetExpanded() {
        getBottomSheetBehavior().ifPresent(behavior -> behavior.setState(BottomSheetBehavior.STATE_EXPANDED));
    }

    private void setBottomSheetSkipCollapsed() {
        getBottomSheetBehavior().ifPresent(behavior -> behavior.setSkipCollapsed(true));
    }

    private Optional<BottomSheetBehavior<?>> getBottomSheetBehavior() {
        return Optional.ofNullable(getDialog())
                .filter(BottomSheetDialog.class::isInstance)
                .map(BottomSheetDialog.class::cast)
                .map(BottomSheetDialog::getBehavior);
    }

    private void showFromBottom(@NonNull View view) {
        view.animate().cancel();
        view.animate()
                .setDuration(ANIM_DURATION)
                .translationY(0)
                .setInterpolator(new AccelerateInterpolator())
                .withStartAction(() -> view.setVisibility(View.VISIBLE))
                .withEndAction(null)
                .start();
    }

    private void hideToBottom(@NonNull View view) {
        view.animate().cancel();
        view.animate()
                .setDuration(ANIM_DURATION)
                .translationY(view.getHeight())
                .setInterpolator(new DecelerateInterpolator())
                .withStartAction(null)
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
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
}
