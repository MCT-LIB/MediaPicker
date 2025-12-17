package com.mct.mediapicker;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.FragmentManager;

import com.mct.mediapicker.fragment.PickerFragment;
import com.mct.mediapicker.fragment.PickerSystemFragment;

public class MediaPicker {

    public static void pick(FragmentManager manager, MediaPickerOption option) {
        //noinspection deprecation
        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable()) {
            PickerSystemFragment.newInstance(option).show(manager, option.getId());
        } else {
            PickerFragment.newInstance(option).show(manager, option.getId());
        }
    }

    private MediaPicker() {
        // no instance
    }
}
