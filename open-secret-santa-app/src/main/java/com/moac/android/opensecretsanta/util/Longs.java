package com.moac.android.opensecretsanta.util;

import java.util.Collection;

public final class Longs {

    private Longs() {
        throw new AssertionError("No instances allowed.");
    }

    public static long[] toArray(Collection<? extends Number> collection) {
        Object[] boxedArray = collection.toArray();
        int len = boxedArray.length;
        long[] array = new long[len];
        for (int i = 0; i < len; i++) {
            // checkNotNull for GWT (do not optimize)
            array[i] = ((Number) Preconditions.checkNotNull(boxedArray[i], "Unexpected null value")).longValue();
        }
        return array;
    }

}
