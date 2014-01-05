package com.moac.android.opensecretsanta.test;

import java.text.SimpleDateFormat;

public class DateTestUtils {

    public static String getDate(String format, long timeMs) {
        return new SimpleDateFormat(format).format(timeMs);
    }
}
