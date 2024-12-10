package com.mct.mediapicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;

import com.google.android.material.color.DynamicColors;

/**
 * Resolves material3 theme for the media picker.
 */
public abstract class M3ThemeStrategy {

    /**
     * Default theme from this library
     */
    public static final M3ThemeStrategy DEFAULT = new M3ThemeStrategy() {
        @NonNull
        @Override
        public Context wrapContext(@NonNull Context context) {
            return fallback(context);
        }
    };

    /**
     * Inherit context theme or fallback to default
     */
    public static final M3ThemeStrategy INHERIT = new M3ThemeStrategy() {
        @NonNull
        @Override
        public Context wrapContext(@NonNull Context context) {
            if (isMaterial3Theme(context.getTheme())) {
                return context;
            }
            return fallback(context);
        }
    };

    /**
     * Dynamic color theme or fallback to default
     */
    public static final M3ThemeStrategy DYNAMIC = new M3ThemeStrategy() {
        @NonNull
        @Override
        public Context wrapContext(@NonNull Context context) {
            if (DynamicColors.isDynamicColorAvailable()) {
                return DynamicColors.wrapContextIfAvailable(context);
            }
            return fallback(context);
        }
    };

    /**
     * Custom theme or fallback to default
     */
    @NonNull
    public static M3ThemeStrategy CUSTOM(int m3ThemeResId) {
        return new M3ThemeStrategy() {
            @NonNull
            @Override
            public Context wrapContext(@NonNull Context context) {
                if (isMaterial3Theme(m3ThemeResId)) {
                    return new ContextThemeWrapperStrategy(m3ThemeResId).wrapContext(context);
                }
                return fallback(context);
            }
        };
    }

    /* --- mix strategies --- */

    /**
     * Inherit context theme or fallback to dynamic color
     */
    public static final M3ThemeStrategy INHERIT_OR_DYNAMIC = new M3ThemeStrategy() {
        @NonNull
        @Override
        public Context wrapContext(@NonNull Context context) {
            return INHERIT.wrapContext(context);
        }

        @NonNull
        @Override
        protected Context fallback(@NonNull Context context) {
            return DYNAMIC.wrapContext(context);
        }
    };

    /**
     * Dynamic color theme or fallback to inherit
     */
    public static final M3ThemeStrategy DYNAMIC_OR_INHERIT = new M3ThemeStrategy() {
        @NonNull
        @Override
        public Context wrapContext(@NonNull Context context) {
            return DYNAMIC.wrapContext(context);
        }

        @NonNull
        @Override
        protected Context fallback(@NonNull Context context) {
            return INHERIT.wrapContext(context);
        }
    };

    /**
     * Inherit context theme or fallback to custom
     */
    @NonNull
    public static M3ThemeStrategy INHERIT_OR_CUSTOM(int m3ThemeResId) {
        return new M3ThemeStrategy() {
            @NonNull
            @Override
            public Context wrapContext(@NonNull Context context) {
                return INHERIT.wrapContext(context);
            }

            @NonNull
            @Override
            protected Context fallback(@NonNull Context context) {
                return CUSTOM(m3ThemeResId).wrapContext(context);
            }
        };
    }

    /**
     * Dynamic color theme or fallback to custom
     */
    @NonNull
    public static M3ThemeStrategy DYNAMIC_OR_CUSTOM(int m3ThemeResId) {
        return new M3ThemeStrategy() {
            @NonNull
            @Override
            public Context wrapContext(@NonNull Context context) {
                return DYNAMIC.wrapContext(context);
            }

            @NonNull
            @Override
            protected Context fallback(@NonNull Context context) {
                return CUSTOM(m3ThemeResId).wrapContext(context);
            }
        };
    }

    /* --- class methods --- */

    @NonNull
    public abstract Context wrapContext(@NonNull Context context);

    @NonNull
    protected Context fallback(@NonNull Context context) {
        return new ContextThemeWrapperStrategy(R.style.PhotoPickerTheme).wrapContext(context);
    }

    private static boolean isMaterial3Theme(int themeRes) {
        Resources.Theme theme = Resources.getSystem().newTheme();
        theme.applyStyle(themeRes, true);
        return isMaterial3Theme(theme);
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

    private static class ContextThemeWrapperStrategy extends M3ThemeStrategy {

        private final int m3ThemeResId;

        private ContextThemeWrapperStrategy(int themeRes) {
            m3ThemeResId = themeRes;
        }

        @NonNull
        @Override
        public Context wrapContext(@NonNull Context context) {
            return new ContextThemeWrapper(context, m3ThemeResId);
        }
    }

}
