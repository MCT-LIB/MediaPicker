package com.mct.mediapicker.common.fastscroll;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

class Utils {

    @ColorInt
    public static int getColorFromAttrRes(@AttrRes int attrRes, @NonNull Context context) {
        ColorStateList colorStateList = getColorStateListFromAttrRes(attrRes, context);
        return colorStateList != null ? colorStateList.getDefaultColor() : 0;
    }

    @Nullable
    public static ColorStateList getColorStateListFromAttrRes(@AttrRes int attrRes,
                                                              @NonNull Context context) {
        TypedArray a = context.obtainStyledAttributes(new int[]{attrRes});
        int resId;
        try {
            resId = a.getResourceId(0, 0);
            if (resId != 0) {
                return ContextCompat.getColorStateList(context, resId);
            }
            return a.getColorStateList(0);
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                a.close();
            } else {
                a.recycle();
            }
        }
    }

}
