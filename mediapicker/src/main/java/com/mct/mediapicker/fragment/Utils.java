package com.mct.mediapicker.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class Utils {

    public static int dp2px(float dpValue) {
        float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5F);
    }

    public static int px2dp(float pxValue) {
        float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5F);
    }

    public static int sp2px(float spValue) {
        float scale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (spValue * scale + 0.5F);
    }

    public static int px2sp(float pxValue) {
        float scale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / scale + 0.5F);
    }

    @SuppressLint("PrivateResource")
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isMaterial3Theme(@NonNull Context context) {
        int attr = com.google.android.material.R.attr.isMaterial3Theme;
        TypedValue typedValue = resolve(context, attr);
        return typedValue != null && typedValue.type == TypedValue.TYPE_INT_BOOLEAN && typedValue.data != 0;
    }

    @Nullable
    private static TypedValue resolve(@NonNull Context context, @AttrRes int attributeResId) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attributeResId, typedValue, true)) {
            return typedValue;
        }
        return null;
    }

    private Utils() {
        //no instance
    }
}
