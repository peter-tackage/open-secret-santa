package com.moac.android.opensecretsanta.types;

import android.provider.BaseColumns;
import com.moac.android.opensecretsanta.activity.Constants;

public class DrawResultEntry implements Comparable<DrawResultEntry> {

    long id = -1;
    String giverName;
    String receiverName;
    int contactMode = Constants.NAME_ONLY_CONTACT_MODE;
    String contactDetail; // the email, the  phone number, the whatever.
    long viewedDate = Constants.UNVIEWED_DATE;
    long sentDate = Constants.UNSENT_DATE;

    public DrawResultEntry(String _giverName, String _receiverName, int _contactMode, String _contactDetail, long _viewedDate, long _sentDate) {
        this.giverName = _giverName;
        this.receiverName = _receiverName;
        this.contactDetail = _contactDetail;
        this.contactMode = _contactMode;
        this.viewedDate = _viewedDate;
        this.sentDate = _sentDate;
    }

    public DrawResultEntry(String _name1, String _name2, int _contactMode, String _contactDetail) {
        this.giverName = _name1;
        this.receiverName = _name2;
        this.contactDetail = _contactDetail;
        this.contactMode = _contactMode;
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

    public long getId() {
        return id;
    }

    public String getGiverName() {
        return giverName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public long getViewedDate() {
        return viewedDate;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setGiverName(String name) {
        this.giverName = name;
    }

    public void setReceiverName(String name) {
        this.receiverName = name;
    }

    public int getContactMode() {
        return contactMode;
    }

    public void setContactMode(int contactMode) {
        this.contactMode = contactMode;
    }

    public String getContactDetail() {
        return contactDetail;
    }

    public void setContactDetail(String contactDetail) {
        this.contactDetail = contactDetail;
    }

    public void setViewedDate(long viewedDate) {
        this.viewedDate = viewedDate;
    }

    @Override
    public int compareTo(DrawResultEntry another) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.giverName, another.giverName);
    }

    public long getSentDate() {
        return sentDate;
    }

    public void setSentDate(long _sentDate) {
        this.sentDate = _sentDate;
    }

    public boolean isSendable() {
        return this.contactMode == Constants.EMAIL_CONTACT_MODE || this.contactMode == Constants.SMS_CONTACT_MODE;
    }
}
