package com.mct.mediapicker.common.fastscroll;

import androidx.annotation.NonNull;

public interface ViewHelperProvider {

    @NonNull
    FastScroller.ViewHelper getViewHelper();
}
