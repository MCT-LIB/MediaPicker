package com.mct.mediapicker.fragment;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.os.ext.SdkExtensions;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mct.mediapicker.MediaPickerOption;

import java.util.Optional;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class PickerSystemFragment extends BottomSheetDialogFragment {

    @NonNull
    public static PickerSystemFragment newInstance(@NonNull MediaPickerOption option) {
        PickerSystemFragment fragment = new PickerSystemFragment();
        fragment.option = option;
        return fragment;
    }

    private MediaPickerOption option;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pickMode = option.getPickMode();
        int pickType = option.getPickType();
        int maxSelection = option.getMaxSelection();

        ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
        ActivityResultContracts.PickVisualMedia.VisualMediaType pickMediaType;

        if (pickMode == MediaPickerOption.PICK_MODE_SINGLE) {
            pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                Optional.ofNullable(option.getListener1()).ifPresent(l -> l.onPick(uri));
                dismiss();
            });
        } else {
            if (ActivityResultContracts.PickVisualMedia.isSystemPickerAvailable$activity_release()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
                    if (maxSelection > MediaStore.getPickImagesMaxLimit()) {
                        maxSelection = MediaStore.getPickImagesMaxLimit();
                    }
                }
            }
            pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(maxSelection), uris -> {
                Optional.ofNullable(option.getListener2()).ifPresent(l -> l.onPick(uris));
                dismiss();
            });
        }

        switch (pickType) {
            case MediaPickerOption.PICK_TYPE_IMAGE:
                pickMediaType = ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE;
                break;
            case MediaPickerOption.PICK_TYPE_VIDEO:
                pickMediaType = ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE;
                break;
            case MediaPickerOption.PICK_TYPE_ALL:
                pickMediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE;
                break;
            default:
                pickMediaType = null;
                break;
        }

        if (pickMediaType != null) {
            pickMediaLauncher.launch(new PickVisualMediaRequest.Builder().setMediaType(pickMediaType).build());
        } else {
            dismiss();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setDimAmount(0);
            window.setElevation(0);
        }

        View overlay = dialog.findViewById(com.google.android.material.R.id.container);
        if (overlay != null) {
            overlay.setVisibility(View.GONE);
        }

        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Presenter.saveOption(option);
        outState.putString("optionId", option.getId());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            option = Presenter.restoredOption(savedInstanceState.getString("optionId"));
        }
    }

}
