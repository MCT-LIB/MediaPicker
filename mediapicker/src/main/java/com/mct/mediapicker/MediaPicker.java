package com.mct.mediapicker;

import android.content.Context;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.mct.mediapicker.fragment.PickerFragment;
import com.mct.mediapicker.fragment.PickerSystemFragment;

import java.util.Objects;

public class MediaPicker {

    public static void pick(@NonNull FragmentActivity activity, @NonNull MediaPickerOption option) {
        Objects.requireNonNull(activity);
        Objects.requireNonNull(option);
        pick(activity.getApplicationContext(), activity.getSupportFragmentManager(), option);
    }

    public static void pick(@NonNull Fragment fragment, @NonNull MediaPickerOption option) {
        Objects.requireNonNull(fragment);
        Objects.requireNonNull(option);
        pick(fragment.requireContext().getApplicationContext(), fragment.getChildFragmentManager(), option);
    }

    private static void pick(Context context, FragmentManager manager, MediaPickerOption option) {
        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
            PickerSystemFragment.newInstance(option).show(manager, option.getId());
        } else {
            PickerFragment.newInstance(option).show(manager, option.getId());
        }
    }

    private MediaPicker() {
        // no instance
    }
}
