package com.moac.android.opensecretsanta.test;

import java.text.SimpleDateFormat;

public class DateTestUtils {

    public static String getDate(String format, long timeMs) {
        return new SimpleDateFormat(format).format(timeMs);
    }

    public static String getGroupLabel(String dateString, long instance) {
        return String.format("My Group %s #%d", dateString, instance);
    }
}
