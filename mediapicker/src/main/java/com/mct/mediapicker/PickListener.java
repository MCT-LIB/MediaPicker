package com.mct.mediapicker;

import androidx.annotation.Nullable;

public interface PickListener<I> {

    void onPick(@Nullable I item);
}
