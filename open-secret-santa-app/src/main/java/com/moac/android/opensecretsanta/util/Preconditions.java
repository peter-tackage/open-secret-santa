package com.moac.android.opensecretsanta.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class Preconditions {

    private Preconditions() {
        throw new AssertionError("No instances allowed.");
    }

    @NonNull
    public static <T> T checkNotNull(@Nullable T value, @NonNull String errorMessage) {
        if (value == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value;
    }

    @NonNull
    public static String checkNotNullOrEmpty(@Nullable String value, @NonNull String errorMessage) {
        if (com.moac.android.opensecretsanta.util.TextUtils.isNullOrEmpty(value)) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value;
    }

    public static void checkState(boolean condition, @NonNull String errorMessage) {
        if (!condition) {
            throw new IllegalStateException(errorMessage);
        }
    }

    @NonNull
    public static <T> T get(@Nullable T value) {
        return checkNotNull(value, "Value cannot be null.");
    }

}
