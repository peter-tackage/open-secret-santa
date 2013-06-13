package com.moac.android.opensecretsanta.adapter;

public class MemberRowDetails implements Comparable<MemberRowDetails> {

    protected long mMemberId;
    protected String mLookupKey;
    protected String mMemberName;
    protected String mContactDetail;
    protected int mContactMode;
    protected long mRestrictionCount;

    public MemberRowDetails(long _memberId, String _lookupKey, String _memberName,
                            int _contactMode, String _contactDetail, long _restrictionCount) {
        mMemberId = _memberId;
        mLookupKey = _lookupKey;
        mMemberName = _memberName;
        mContactMode = _contactMode;
        mContactDetail = _contactDetail;
        mRestrictionCount = _restrictionCount;
    }


    public long getMemberId() { return mMemberId; }
    public String getMemberName() { return mMemberName; }
    public String getLookupKey() { return mLookupKey; }

    @Override
    public int compareTo(MemberRowDetails _that) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.mMemberName, _that.mMemberName);
    }
}
