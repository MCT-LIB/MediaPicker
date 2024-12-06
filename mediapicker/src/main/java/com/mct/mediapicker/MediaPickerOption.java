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
    private final int maxSelection;
    private final PickListener<Uri> listener1;
    private final PickListener<List<Uri>> listener2;

    MediaPickerOption(@NonNull Builder builder) {
        id = UUID.randomUUID().toString();
        pickMode = builder.pickMode;
        pickType = builder.pickType;
        maxSelection = builder.maxSelection;
        listener1 = builder.listener1;
        listener2 = builder.listener2;
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

    public static class Builder {

        private int pickMode;
        private int pickType;
        private int maxSelection;
        private PickListener<Uri> listener1;
        private PickListener<List<Uri>> listener2;

        public Builder() {
            set(PICK_MODE_SINGLE, 1, null, null);
            pickType = PICK_TYPE_IMAGE;
        }

        public Builder all() {
            pickType = PICK_TYPE_ALL;
            return this;
        }

        public Builder image() {
            pickType = PICK_TYPE_IMAGE;
            return this;
        }

        public Builder video() {
            pickType = PICK_TYPE_VIDEO;
            return this;
        }

        public Builder single(PickListener<Uri> listener) {
            set(PICK_MODE_SINGLE, 1, listener, null);
            return this;
        }

        public Builder multi(PickListener<List<Uri>> listener) {
            set(PICK_MODE_MULTI, Integer.MAX_VALUE, null, listener);
            return this;
        }

        public Builder multi(PickListener<List<Uri>> listener, @IntRange(from = 1) int maxSelection) {
            set(PICK_MODE_MULTI, maxSelection, null, listener);
            return this;
        }

        public MediaPickerOption build() {
            return new MediaPickerOption(this);
        }

        private void set(int pickMode, int maxSelectionCount, PickListener<Uri> listener1, PickListener<List<Uri>> listener2) {
            this.pickMode = pickMode;
            this.maxSelection = maxSelectionCount;
            this.listener1 = listener1;
            this.listener2 = listener2;
        }
    }

}
