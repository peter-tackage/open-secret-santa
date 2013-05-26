package com.moac.android.opensecretsanta.types;

import android.provider.BaseColumns;
import com.moac.android.opensecretsanta.activity.Constants;

public class DrawResultEntry extends PersistentModel implements Comparable<DrawResultEntry> {

    private String mGiverName;
    private String mReceiverName;
    private int mContactMode = Constants.NAME_ONLY_CONTACT_MODE;
    private String mContactDetail; // the email, the  phone number, the whatever.
    private long mViewedDate = Constants.UNVIEWED_DATE;
    private long mSentDate = Constants.UNSENT_DATE;

    public DrawResultEntry(String _giverName, String _receiverName, int _contactMode, String _contactDetail, long _viewedDate, long _sentDate) {
        mGiverName = _giverName;
        mReceiverName = _receiverName;
        mContactDetail = _contactDetail;
        mContactMode = _contactMode;
        mViewedDate = _viewedDate;
        mSentDate = _sentDate;
    }

    public DrawResultEntry(String _name1, String _name2, int _contactMode, String _contactDetail) {
        mGiverName = _name1;
        mReceiverName = _name2;
        mContactDetail = _contactDetail;
        mContactMode = _contactMode;
    }

    public static interface DrawResultEntryColumns extends BaseColumns {

        // For entries in that result definition.
        public static final String DRAW_RESULT_ID_COLUMN = "DRAW_RESULT_ID";
        public static final String MEMBER_NAME_COLUMN = "MEMBER_NAME";
        public static final String OTHER_MEMBER_NAME_COLUMN = "OTHER_MEMBER_NAME";
        public static final String CONTACT_MODE_COLUMN = "CONTACT_MODE";
        public static final String CONTACT_DETAIL_COLUMN = "CONTACT_DETAIL";
        public static final String VIEWED_DATE_COLUMN = "VIEWED_DATE";
        public static final String SENT_DATE_COLUMN = "SENT_DATE";

        public static final String DEFAULT_SORT_ORDER = MEMBER_NAME_COLUMN + " ASC";

        public static String[] ALL = { _ID, DRAW_RESULT_ID_COLUMN, MEMBER_NAME_COLUMN,
          OTHER_MEMBER_NAME_COLUMN, CONTACT_MODE_COLUMN, CONTACT_DETAIL_COLUMN, VIEWED_DATE_COLUMN, SENT_DATE_COLUMN };
    }

    public String getGiverName() { return mGiverName;}

    public String getReceiverName() { return mReceiverName; }

    public long getViewedDate() { return mViewedDate; }

    public void setGiverName(String name) { mGiverName = name; }

    public void setReceiverName(String name) { mReceiverName = name; }

    public int getContactMode() { return mContactMode; }

    public void setContactMode(int contactMode) { mContactMode = contactMode; }

    public String getContactDetail() { return mContactDetail; }

    public void setContactDetail(String contactDetail) { mContactDetail = contactDetail; }

    public void setViewedDate(long viewedDate) { mViewedDate = viewedDate; }

    @Override
    public int compareTo(DrawResultEntry another) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.mGiverName, another.mGiverName);
    }

    public long getSentDate() { return mSentDate; }

    public void setSentDate(long _sentDate) { mSentDate = _sentDate; }

    public boolean isSendable() {
        return mContactMode == Constants.EMAIL_CONTACT_MODE || mContactMode == Constants.SMS_CONTACT_MODE;
    }
}
