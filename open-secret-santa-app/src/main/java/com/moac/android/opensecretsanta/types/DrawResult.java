package com.moac.android.opensecretsanta.types;

import android.provider.BaseColumns;
import com.moac.android.opensecretsanta.activity.Constants;

public class DrawResult extends PersistentModel {

    private long mDrawDate = Constants.UNDRAWN_DATE;
    private long mSendDate = Constants.UNSENT_DATE;
    private String mMessage = "";

    public static interface DrawResultColumns extends BaseColumns {

        // Results definition
        public static final String DRAW_DATE_COLUMN = "DRAW_DATE";
        public static final String SEND_DATE_COLUMN = "SEND_DATE";
        public static final String MESSAGE_COLUMN = "MESSAGE";
        public static final String GROUP_ID_COLUMN = "GROUP_ID";

        public static final String DEFAULT_SORT_ORDER = DRAW_DATE_COLUMN + " DESC";

        public static String[] ALL = { _ID, DRAW_DATE_COLUMN, SEND_DATE_COLUMN, MESSAGE_COLUMN, GROUP_ID_COLUMN };
    }

    public long getDrawDate() { return mDrawDate; }
    public void setDrawDate(long _drawDate) { mDrawDate = _drawDate; }
    public void setSendDate(long sendDate) { mSendDate = sendDate; }
    public long getSendDate() { return mSendDate; }
    public void setMessage(String message) { mMessage = message; }
    public String getMessage() { return mMessage; }
}
