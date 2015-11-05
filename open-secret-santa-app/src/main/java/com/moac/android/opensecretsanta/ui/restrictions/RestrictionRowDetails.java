package com.moac.android.opensecretsanta.ui.restrictions;

import android.support.annotation.NonNull;

class RestrictionRowDetails implements Comparable<RestrictionRowDetails> {

    protected long mFromMemberId;
    protected long mToMemberId;
    protected String mToMemberName;
    protected boolean mIsRestricted;
    protected long mContactId;
    protected String mLookupKey;

    public boolean isRestricted() { return mIsRestricted; }
    public void setRestricted(boolean _restricted) { mIsRestricted = _restricted; }

    public long getFromMemberId() { return mFromMemberId; }
    public void setFromMemberId(long fromMemberId) { mFromMemberId = fromMemberId;}

    public void setToMemberId(long toMemberId) { mToMemberId = toMemberId; }
    public long getToMemberId() { return mToMemberId; }

    public void setToMemberName(String toMemberName) { mToMemberName = toMemberName; }
    public String getToMemberName() { return mToMemberName; }

    public String getLookupKey() { return mLookupKey;}
    public void setLookupKey(String lookupKey) { mLookupKey = lookupKey; }

    public long getContactId() { return mContactId; }
    public void setContactId(long contactId) { mContactId = contactId; }

    @Override
    public int compareTo(@NonNull RestrictionRowDetails another) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.mToMemberName, another.mToMemberName);
    }
}