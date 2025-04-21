package com.mct.mediapicker;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.mct.mediapicker.MediaPickerOption.PICK_TYPE_ALL;
import static com.mct.mediapicker.MediaPickerOption.PICK_TYPE_IMAGE;
import static com.mct.mediapicker.MediaPickerOption.PICK_TYPE_VIDEO;
import static com.mct.mediapicker.MediaPickerOption.PickType;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * @noinspection RedundantIfStatement
 */
public class MediaUtils {

    @NonNull
    public static String[] getRequestPermissions(@PickType int type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            switch (type) {
                case PICK_TYPE_IMAGE:
                    return new String[]{READ_MEDIA_IMAGES, READ_MEDIA_VISUAL_USER_SELECTED};
                case PICK_TYPE_VIDEO:
                    return new String[]{READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED};
                case PICK_TYPE_ALL:
                    return new String[]{READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED};
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            switch (type) {
                case PICK_TYPE_IMAGE:
                    return new String[]{READ_MEDIA_IMAGES};
                case PICK_TYPE_VIDEO:
                    return new String[]{READ_MEDIA_VIDEO};
                case PICK_TYPE_ALL:
                    return new String[]{READ_MEDIA_IMAGES, READ_MEDIA_VIDEO};
            }
        }
        return new String[]{READ_EXTERNAL_STORAGE};
    }

    public static boolean isPermissionsGranted(Context context, @PickType int type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            // Full access via all files manager
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Full access on Android 13 (API level 33) or higher
            boolean granted = false;
            switch (type) {
                case MediaPickerOption.PICK_TYPE_IMAGE:
                    granted = isGranted(context, READ_MEDIA_IMAGES);
                    break;
                case MediaPickerOption.PICK_TYPE_VIDEO:
                    granted = isGranted(context, READ_MEDIA_VIDEO);
                    break;
                case MediaPickerOption.PICK_TYPE_ALL:
                    granted = isGranted(context, READ_MEDIA_IMAGES) && isGranted(context, READ_MEDIA_VIDEO);
                    break;
            }
            if (granted) {
                return true;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && isGranted(context, READ_MEDIA_VISUAL_USER_SELECTED)) {
            // Partial access on Android 14 (API level 34) or higher
            return true;
        }
        if (isGranted(context, READ_EXTERNAL_STORAGE)) {
            // Full access up to Android 12 (API level 32)
            return true;
        }
        // Access denied
        return false;
    }

    public static boolean shouldReselection(Context context, @PickType int type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            // Full access via all files manager
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Full access on Android 13 (API level 33) or higher
            boolean granted = false;
            switch (type) {
                case MediaPickerOption.PICK_TYPE_IMAGE:
                    granted = isGranted(context, READ_MEDIA_IMAGES);
                    break;
                case MediaPickerOption.PICK_TYPE_VIDEO:
                    granted = isGranted(context, READ_MEDIA_VIDEO);
                    break;
                case MediaPickerOption.PICK_TYPE_ALL:
                    granted = isGranted(context, READ_MEDIA_IMAGES) && isGranted(context, READ_MEDIA_VIDEO);
                    break;
            }
            if (granted) {
                return false;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && isGranted(context, READ_MEDIA_VISUAL_USER_SELECTED)) {
            // Partial access on Android 14 (API level 34) or higher
            return true;
        }
        return false;
    }

    public static boolean shouldGoToSettings(Activity activity, @PickType int type) {
        for (String permission : getRequestPermissions(type)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return false;
            }
        }
        return true;
    }

    public static void gotoSettings(Activity activity) {
        if (activity != null) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
        }
    }

    private static boolean isGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED;
    }

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public static void runOnUiThread(Runnable runnable) {
        MAIN_HANDLER.post(runnable);
    }

    public static void runOnUiThreadDelayed(Runnable runnable, long delayMillis) {
        MAIN_HANDLER.postDelayed(runnable, delayMillis);
    }

    public static void removeOnUiThread(Runnable runnable) {
        MAIN_HANDLER.removeCallbacks(runnable);
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

    private MediaUtils() {
        //no instance
    }

}
