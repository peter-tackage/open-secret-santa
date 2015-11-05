package com.moac.android.opensecretsanta.util;

import android.support.annotation.Nullable;

public final class TextUtils {

    private TextUtils() {
        throw new AssertionError("No instances allowed.");
    }

    public static boolean isNullOrEmpty(@Nullable String string) {
        return string == null || string.isEmpty();
    }
}
