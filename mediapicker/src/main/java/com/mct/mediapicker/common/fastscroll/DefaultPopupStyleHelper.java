package com.mct.mediapicker.common.fastscroll;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mct.mediapicker.R;

class DefaultPopupStyleHelper implements FastScroller.PopupStyleHelper {

    @Override
    public void apply(@NonNull TextView popupView) {
        Context context = popupView.getContext();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) popupView.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMarginEnd(Utils.dp2px(12));
        popupView.setLayoutParams(layoutParams);
        popupView.setBackgroundResource(R.drawable.fs_popup);
        popupView.setElevation(Utils.dp2px(3));
        popupView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        popupView.setGravity(Gravity.CENTER);
        popupView.setIncludeFontPadding(false);
        popupView.setSingleLine(true);
        popupView.setTextColor(Utils.getColorFromAttrRes(context, android.R.attr.textColorPrimaryInverse));
        popupView.setTextSize(12f);
        popupView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    }
}
