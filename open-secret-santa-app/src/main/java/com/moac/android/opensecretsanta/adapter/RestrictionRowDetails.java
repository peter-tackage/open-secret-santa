package com.moac.android.opensecretsanta.adapter;

public class RestrictionRowDetails implements Comparable<RestrictionRowDetails> {

    protected long mFromMemberId;
    protected long mToMemberId;
    protected String mToMemberName;
    protected boolean mIsRestricted;

    public RestrictionRowDetails(){}

    public RestrictionRowDetails(long _fromMemberId, long _toMemberId, String _toMemberName, boolean _isRestricted) {
        this.mFromMemberId = _fromMemberId;
        this.mToMemberId = _toMemberId;
        this.mToMemberName = _toMemberName;
        this.mIsRestricted = _isRestricted;
    }

    public boolean isRestricted() { return mIsRestricted; }
    public void setRestricted(boolean _restricted) { mIsRestricted = _restricted; }

    public long getFromMemberId() { return mFromMemberId; }
    public void setFromMemberId(long fromMemberId) { mFromMemberId = fromMemberId;}

    public void setToMemberId(long toMemberId) { mToMemberId = toMemberId; }
    public long getToMemberId() { return mToMemberId; }

    public void setToMemberName(String toMemberName) { mToMemberName = toMemberName; }
    public String getToMemberName() { return mToMemberName; }

    @Override
    public int compareTo(RestrictionRowDetails another) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.mToMemberName, another.mToMemberName);
    }
}