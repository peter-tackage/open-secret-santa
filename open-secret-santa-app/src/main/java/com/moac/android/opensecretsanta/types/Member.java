package com.moac.android.opensecretsanta.types;

import android.provider.BaseColumns;
import android.util.Log;
import com.moac.android.opensecretsanta.activity.Constants;

public final class Member {

    public static final String TAG = "Member";

    public static final class MemberColumns implements BaseColumns {

        public static final String LOOKUP_KEY = "LOOKUP_KEY";
        public static final String NAME_COLUMN = "NAME";
        public static final String CONTACT_MODE_COLUMN = "CONTACT_MODE";
        public static final String CONTACT_DETAIL_COLUMN = "CONTACT_DETAIL";
        public static final String GROUP_ID_COLUMN = "GROUP_ID";

        public static final String DEFAULT_SORT_ORDER = NAME_COLUMN + " ASC";

        public static final String[] ALL = {
          _ID,
          LOOKUP_KEY,
          NAME_COLUMN,
          CONTACT_MODE_COLUMN,
          CONTACT_DETAIL_COLUMN,
          GROUP_ID_COLUMN
        };
    }

    public static final class RestrictionsColumns implements BaseColumns {

        public static final String MEMBER_ID_COLUMN = "MEMBER_ID";
        public static final String OTHER_MEMBER_ID_COLUMN = "OTHER_MEMBER_ID";

        public static final String[] ALL = {
          _ID,
          MEMBER_ID_COLUMN,
          OTHER_MEMBER_ID_COLUMN
        };

        public static final String DEFAULT_SORT_ORDER = OTHER_MEMBER_ID_COLUMN + " DESC";
    }

    // Member variables.
    private long mId = -1;

    private String mLookupKey;

    // The participant name
    private String mName;

    private String mContactDetail; // the email, the  phone number, the whatever.

    // The types of communication to be used.
    private int mContactMode = Constants.NAME_ONLY_CONTACT_MODE;

    /*
     * Methods
     */
    public Member() {
    }

    public Member(long id, String name, String lookupKey, String contact, int mode) {
        this.mId = id;
        this.mName = name;
        this.mLookupKey = lookupKey;
        this.mContactDetail = contact;
        this.mContactMode = mode;
    }

    public String getName() {
        return mName;
    }

    public void setName(String _name) {
        this.mName = _name;
    }

    public String getContactDetail() {
        return mContactDetail;
    }

    public void setContactDetail(String _contactDetail) {
        mContactDetail = _contactDetail;
    }

    public int getContactMode() {
        return mContactMode;
    }

    public void setContactMode(int _contactMode) {
        mContactMode = _contactMode;
    }

    public long getId() {
        return mId;
    }

    public void setId(long _id) {
        mId = _id;
    }

    public String getLookupKey() {
        return mLookupKey;
    }

    public void setLookupKey(String _lookupKey) {
        this.mLookupKey = _lookupKey;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Id: ");
        sb.append(getId());
        sb.append(", Name: ");
        sb.append(getName());
        sb.append(", Contact Detail: ");
        sb.append(getContactDetail());
        sb.append(", Contact Mode: ");
        sb.append(getContactMode());
        sb.append(", Lookup Key: ");
        sb.append(getLookupKey());

        return sb.toString();
    }

    @Override
    public boolean equals(Object other) {
        if(this == other) return true;

        if(!(other instanceof Member)) return false;

        Member that = (Member) other;
        Log.v(TAG, "This - " + toString());
        Log.v(TAG, "That - " + that.toString());
        return
          (null == this.mContactDetail ? (this.mContactDetail == that.mContactDetail) : this.mContactDetail.equals(that.mContactDetail))
            &&
            (null == this.mLookupKey ? (this.mLookupKey == that.mLookupKey) : this.mLookupKey.equals(that.mLookupKey))
            &&
            (null == this.mName ? (this.mName == that.mName) : this.mName.equals(that.mName))
            &&
            this.mContactMode == that.mContactMode;
    }
}
