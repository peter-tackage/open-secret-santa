package com.moac.android.opensecretsanta.types;

import android.provider.BaseColumns;
import com.moac.android.opensecretsanta.activity.Constants;

public class DrawResult {

    private long mId = -1;
    private long mDrawDate = Constants.UNDRAWN_DATE;
    private long mSendDate = Constants.UNSENT_DATE;
    private String mMessage = "";

    @SuppressWarnings("unused")
    private DrawResult() {}

    ;

    public DrawResult(long _drawDate) {
        this.mDrawDate = _drawDate;
    }

    public DrawResult(long _id, long _drawDate, long _sendDate, String _message) {
        this.mId = _id;
        this.mDrawDate = _drawDate;
        this.mSendDate = _sendDate;
        this.mMessage = _message;
    }

    public static final class DrawResultColumns implements BaseColumns {

        private DrawResultColumns() {}

        ;

        // Results definition
        public static final String DRAW_DATE_COLUMN = "DRAW_DATE";
        public static final String SEND_DATE_COLUMN = "SEND_DATE";
        public static final String MESSAGE_COLUMN = "MESSAGE";
        public static final String GROUP_ID_COLUMN = "GROUP_ID";

        public static final String DEFAULT_SORT_ORDER = DRAW_DATE_COLUMN + " DESC";
        ;

        public static String[] ALL = { _ID, DRAW_DATE_COLUMN, SEND_DATE_COLUMN, MESSAGE_COLUMN, GROUP_ID_COLUMN };
    }

    public long getDrawDate() {
        return mDrawDate;
    }

    public void setDrawDate(long _drawDate) {
        mDrawDate = _drawDate;
    }

    public long getId() {
        return mId;
    }

    public void setId(long _id) {
        mId = _id;
    }

    public void setSendDate(long sendDate) {
        this.mSendDate = sendDate;
    }

    public long getSendDate() {
        return mSendDate;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public String getMessage() {
        return mMessage;
    }
}
