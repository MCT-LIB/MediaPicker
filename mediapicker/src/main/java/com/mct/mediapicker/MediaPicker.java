package com.mct.mediapicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.mct.mediapicker.fragment.PickerFragment;

import java.util.Objects;

public class MediaPicker {

    public static void pick(@NonNull FragmentManager manager, @NonNull MediaPickerOption option) {
        Objects.requireNonNull(manager);
        Objects.requireNonNull(option);
        PickerFragment.newInstance(option).show(manager, option.getId());
    }

    private MediaPicker() {
        // no instance
    }
}
