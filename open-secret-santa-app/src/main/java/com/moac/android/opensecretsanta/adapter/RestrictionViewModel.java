package com.moac.android.opensecretsanta.adapter;

public class RestrictionViewModel implements Comparable<RestrictionViewModel> {

    private long mToMemberId;
    private String mToMemberName;
    private boolean mIsRestricted;
    private long mContactId;
    private String mLookupKey;

    public RestrictionViewModel(long toMemberId, String toMemberName, boolean isRestricted,
                                long contactId, String lookupKey) {
        this.mToMemberId = toMemberId;
        this.mToMemberName = toMemberName;
        this.mIsRestricted = isRestricted;
        this.mContactId = contactId;
        this.mLookupKey = lookupKey;
    }

    public boolean isRestricted() {
        return mIsRestricted;
    }

    public long getToMemberId() {
        return mToMemberId;
    }

    public String getToMemberName() {
        return mToMemberName;
    }

    public String getLookupKey() {
        return mLookupKey;
    }

    public long getContactId() {
        return mContactId;
    }

    @Override
    public int compareTo(RestrictionViewModel another) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.mToMemberName, another.mToMemberName);
    }
}