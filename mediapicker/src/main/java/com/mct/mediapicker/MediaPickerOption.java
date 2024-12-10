package com.mct.mediapicker;

import android.net.Uri;

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.UUID;

public class MediaPickerOption {

    public static final int PICK_MODE_SINGLE = 0;
    public static final int PICK_MODE_MULTI = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PICK_MODE_SINGLE, PICK_MODE_MULTI})
    public @interface PickMode {
    }

    public static final int PICK_TYPE_IMAGE = 0;
    public static final int PICK_TYPE_VIDEO = 1;
    public static final int PICK_TYPE_ALL = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PICK_TYPE_IMAGE, PICK_TYPE_VIDEO, PICK_TYPE_ALL})
    public @interface PickType {
    }

    private final String id;
    private final int pickMode;
    private final int pickType;
    private final int minSelection;
    private final int maxSelection;
    private final PickListener<Uri> listener1;
    private final PickListener<List<Uri>> listener2;
    private final M3ThemeStrategy themeStrategy;

    MediaPickerOption(@NonNull Builder builder) {
        id = UUID.randomUUID().toString();
        pickMode = builder.pickMode;
        pickType = builder.pickType;
        minSelection = builder.minSelection;
        maxSelection = builder.maxSelection;
        listener1 = builder.listener1;
        listener2 = builder.listener2;
        themeStrategy = builder.themeStrategy;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @PickMode
    public int getPickMode() {
        return pickMode;
    }

    @PickType
    public int getPickType() {
        return pickType;
    }

    public int getMinSelection() {
        return minSelection;
    }

    public int getMaxSelection() {
        return maxSelection;
    }

    @Nullable
    public PickListener<Uri> getListener1() {
        return listener1;
    }

    @Nullable
    public PickListener<List<Uri>> getListener2() {
        return listener2;
    }

    public M3ThemeStrategy getThemeStrategy() {
        return themeStrategy;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {

        private int pickMode;
        private int pickType;
        private int minSelection;
        private int maxSelection;
        private PickListener<Uri> listener1;
        private PickListener<List<Uri>> listener2;
        private M3ThemeStrategy themeStrategy;

        public Builder() {
            single(null).image();
            themeStrategy = M3ThemeStrategy.DYNAMIC_OR_INHERIT;
        }

        /**
         * Pick image only
         */
        public Builder image() {
            pickType = PICK_TYPE_IMAGE;
            return this;
        }

        /**
         * Pick video only
         */
        public Builder video() {
            pickType = PICK_TYPE_VIDEO;
            return this;
        }

        /**
         * Pick image and video
         */
        public Builder all() {
            pickType = PICK_TYPE_ALL;
            return this;
        }

        /**
         * Pick single media
         */
        public Builder single(PickListener<Uri> listener) {
            set(PICK_MODE_SINGLE, 1, 1, listener, null);
            return this;
        }

        /**
         * Pick multiple media
         */
        public Builder multi(PickListener<List<Uri>> listener) {
            return multiRange(listener, 1, Integer.MAX_VALUE);
        }

        /**
         * Pick multiple media with exact selection
         */
        public Builder multiExact(PickListener<List<Uri>> listener, int exactSelection) {
            return multiRange(listener, exactSelection, exactSelection);
        }

        /**
         * Pick multiple media with min and max selection
         */
        public Builder multiRange(
                PickListener<List<Uri>> listener,
                @IntRange(from = 1) int minSelection,
                @IntRange(from = 1) int maxSelection) {
            set(PICK_MODE_MULTI, minSelection, maxSelection, null, listener);
            return this;
        }

        /**
         * Theme strategy for this picker
         *
         * @see M3ThemeStrategy
         */
        public Builder themeStrategy(@NonNull M3ThemeStrategy themeStrategy) {
            this.themeStrategy = themeStrategy;
            return this;
        }

        public MediaPickerOption build() {
            return new MediaPickerOption(this);
        }

        private void set(
                int pickMode,
                int minSelection,
                int maxSelection,
                PickListener<Uri> listener1,
                PickListener<List<Uri>> listener2) {
            if (minSelection < 1) {
                throw new IllegalArgumentException("minSelection must be >= 1");
            }
            if (minSelection > maxSelection) {
                throw new IllegalArgumentException("minSelection must be <= maxSelection");
            }
            this.pickMode = pickMode;
            this.minSelection = minSelection;
            this.maxSelection = maxSelection;
            this.listener1 = listener1;
            this.listener2 = listener2;
        }
    }

}
