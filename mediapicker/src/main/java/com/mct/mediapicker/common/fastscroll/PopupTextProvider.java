package com.mct.mediapicker.common.fastscroll;

import android.view.View;

import androidx.annotation.NonNull;

public interface PopupTextProvider {

    CharSequence getPopupText(@NonNull View view, int position);
}
