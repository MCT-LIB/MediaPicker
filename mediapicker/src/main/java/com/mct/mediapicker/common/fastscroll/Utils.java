package com.mct.mediapicker.common.fastscroll;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

class Utils {

    @ColorInt
    public static int getColorFromAttrRes(@NonNull Context context, @AttrRes int attrRes) {
        ColorStateList colorStateList = getColorStateListFromAttrRes(context, attrRes);
        return colorStateList != null ? colorStateList.getDefaultColor() : 0;
    }

    @Nullable
    public static ColorStateList getColorStateListFromAttrRes(@NonNull Context context, @AttrRes int attrRes) {
        TypedArray a = context.obtainStyledAttributes(new int[]{attrRes});
        try {
            int resId = a.getResourceId(0, 0);
            if (resId != 0) {
                return ContextCompat.getColorStateList(context, resId);
            } else {
                return a.getColorStateList(0);
            }
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                a.close();
            } else {
                a.recycle();
            }
        }
    }

    public static int dp2px(float dpValue) {
        float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5F);
    }

}
