package com.mct.mediapicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.view.ContextThemeWrapper;

import com.google.android.material.color.DynamicColors;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves material3 theme for the media picker.
 */
public abstract class M3ThemeStrategy {

    /* --- base strategies --- */

    /**
     * Default theme from this library
     */
    public static M3ThemeStrategy DEFAULT = theme(R.style.PhotoPickerTheme);

    /**
     * Inherit context theme or fallback to default
     */
    public static final M3ThemeStrategy INHERIT = new M3ThemeStrategy() {
        @Override
        protected @NonNull Context handle(@NonNull Context context) {
            return context;
        }

        @Override
        protected boolean canHandle(@NonNull Context context) {
            return isMaterial3Theme(context.getTheme());
        }
    };

    /**
     * Dynamic color theme or fallback to default
     */
    public static final M3ThemeStrategy DYNAMIC = new M3ThemeStrategy() {
        @Override
        protected @NonNull Context handle(@NonNull Context context) {
            return DynamicColors.wrapContextIfAvailable(context);
        }

        @Override
        protected boolean canHandle(@NonNull Context context) {
            return DynamicColors.isDynamicColorAvailable();
        }
    };

    /**
     * Custom material3 theme or fallback to default
     */
    @NonNull
    public static M3ThemeStrategy CUSTOM(@StyleRes int m3ThemeResId) {
        return theme(m3ThemeResId);
    }

    /* --- mix strategies --- */

    /**
     * Inherit context theme or fallback to dynamic color
     */
    public static final M3ThemeStrategy INHERIT_OR_DYNAMIC = composite(INHERIT, DYNAMIC);

    /**
     * Dynamic color theme or fallback to inherit
     */
    public static final M3ThemeStrategy DYNAMIC_OR_INHERIT = composite(DYNAMIC, INHERIT);

    /**
     * Inherit context theme or fallback to custom
     */
    @NonNull
    public static M3ThemeStrategy INHERIT_OR_CUSTOM(int m3ThemeResId) {
        return composite(INHERIT, CUSTOM(m3ThemeResId));
    }

    /**
     * Dynamic color theme or fallback to custom
     */
    @NonNull
    public static M3ThemeStrategy DYNAMIC_OR_CUSTOM(int m3ThemeResId) {
        return composite(DYNAMIC, CUSTOM(m3ThemeResId));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Class methods
    ///////////////////////////////////////////////////////////////////////////

    @NonNull
    public final Context wrapContext(@NonNull Context context) {
        return composite(this, DEFAULT).handle(context);
    }

    @NonNull
    protected abstract Context handle(@NonNull Context context);

    protected abstract boolean canHandle(@NonNull Context context);

    ///////////////////////////////////////////////////////////////////////////
    // Helper methods
    ///////////////////////////////////////////////////////////////////////////

    @NonNull
    private static M3ThemeStrategy theme(@StyleRes int m3ThemeResId) {
        return new M3ThemeStrategy() {
            @Override
            protected @NonNull Context handle(@NonNull Context context) {
                return new ContextThemeWrapper(context, m3ThemeResId);
            }

            @Override
            protected boolean canHandle(@NonNull Context context) {
                return isMaterial3Theme(context, m3ThemeResId);
            }
        };
    }

    @NonNull
    private static M3ThemeStrategy composite(M3ThemeStrategy... strategies) {
        return new M3ThemeStrategy() {
            @Override
            protected @NonNull Context handle(@NonNull Context context) {
                for (M3ThemeStrategy strategy : strategies) {
                    if (strategy.canHandle(context)) {
                        return strategy.handle(context);
                    }
                }
                return context;
            }

            @Override
            protected boolean canHandle(@NonNull Context context) {
                for (M3ThemeStrategy strategy : strategies) {
                    if (strategy.canHandle(context)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    private static final Map<Integer, Boolean> themeCache = new HashMap<>();

    private static boolean isMaterial3Theme(Context context, int themeRes) {
        Boolean result = themeCache.get(themeRes);
        if (result != null) {
            return result;
        }
        Resources.Theme theme = context.getResources().newTheme();
        theme.applyStyle(themeRes, true);
        result = isMaterial3Theme(theme);
        themeCache.put(themeRes, result);
        return result;
    }

    @SuppressLint("PrivateResource")
    private static boolean isMaterial3Theme(@NonNull Resources.Theme theme) {
        int attr = com.google.android.material.R.attr.isMaterial3Theme;
        TypedValue typedValue = new TypedValue();
        if (theme.resolveAttribute(attr, typedValue, true)) {
            return typedValue.type == TypedValue.TYPE_INT_BOOLEAN && typedValue.data != 0;
        }
        return false;
    }

}
