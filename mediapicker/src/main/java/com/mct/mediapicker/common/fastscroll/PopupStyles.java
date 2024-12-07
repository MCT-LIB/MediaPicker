package com.mct.mediapicker.common.fastscroll;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.util.Consumer;

import com.mct.mediapicker.R;

public class PopupStyles {

    public static Consumer<TextView> MD2 = popupView -> {
        Resources resources = popupView.getResources();
        popupView.setMinimumWidth(resources.getDimensionPixelSize(
                R.dimen.afs_md2_popup_min_width));
        popupView.setMinimumHeight(resources.getDimensionPixelSize(
                R.dimen.afs_md2_popup_min_height));
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)
                popupView.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        layoutParams.setMarginEnd(resources.getDimensionPixelOffset(
                R.dimen.afs_md2_popup_margin_end));
        popupView.setLayoutParams(layoutParams);
        Context context = popupView.getContext();
        popupView.setBackground(new Md2PopupBackground(context));
        popupView.setElevation(resources.getDimensionPixelOffset(R.dimen.afs_md2_popup_elevation));
        popupView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        popupView.setGravity(Gravity.CENTER);
        popupView.setIncludeFontPadding(false);
        popupView.setSingleLine(true);
        popupView.setTextColor(Utils.getColorFromAttrRes(android.R.attr.textColorPrimaryInverse,
                context));
        popupView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(
                R.dimen.afs_md2_popup_text_size));
    };

    private PopupStyles() {
    }
}
