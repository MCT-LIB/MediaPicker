package com.mct.mediapicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.util.TypedValue;
import android.view.WindowManager;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class MediaUtils {

    public static boolean canLoadImages(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                return true;
            }
        }
        return isGranted(context, Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static boolean canLoadVideos(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                return true;
            }
        }
        return isGranted(context, Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_VIDEO
                : Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static boolean canLoadMedia(Context context) {
        return canLoadImages(context) && canLoadVideos(context);
    }

    @NonNull
    public static String[] getImagesPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
    }

    @NonNull
    public static String[] getVideosPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.READ_MEDIA_VIDEO};
        } else {
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
    }

    @NonNull
    public static String[] getMediaPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO};
        } else {
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
    }

    private static boolean isGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static int getScreenWidth(@NonNull Context context) {
        return getScreenSize(context).x;
    }

    public static int getScreenHeight(@NonNull Context context) {
        return getScreenSize(context).y;
    }

    private static Point screenSize;

    private static Point getScreenSize(@NonNull Context context) {
        if (screenSize == null) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getSize(screenSize = new Point());
        }
        return screenSize;
    }

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

    private MediaUtils() {
        //no instance
    }

}
