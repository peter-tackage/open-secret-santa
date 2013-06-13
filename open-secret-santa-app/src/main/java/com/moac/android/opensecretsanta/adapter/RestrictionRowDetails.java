package com.moac.android.opensecretsanta.adapter;

public class RestrictionRowDetails implements Comparable<RestrictionRowDetails> {

    protected long mFromMemberId;
    protected long mToMemberId;
    protected String mToMemberName;
    protected boolean mIsRestricted;

    public RestrictionRowDetails(long fromMemberId, long toMemberId, String toMemberName, boolean restricted) {
        this.mFromMemberId = fromMemberId;
        this.mToMemberId = toMemberId;
        this.mToMemberName = toMemberName;
        this.mIsRestricted = restricted;
    }

    public boolean isRestricted() { return mIsRestricted; }
    public void setRestricted(boolean _restricted) { mIsRestricted = _restricted; }
    public long getFromMemberId() { return mFromMemberId; }
    public long getToMemberId() { return mToMemberId; }

    public String getToMemberName() { return mToMemberName; }

    @Override
    public int compareTo(RestrictionRowDetails another) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.mToMemberName, another.mToMemberName);
    }
}