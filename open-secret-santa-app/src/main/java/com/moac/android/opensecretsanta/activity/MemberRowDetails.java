package com.moac.android.opensecretsanta.activity;

public class MemberRowDetails implements Comparable<MemberRowDetails> {

    public MemberRowDetails(long memberId, String _lookupKey, String memberName, int contactMode, String contactDetail, long _restrictionCount) {
        this.memberId = memberId;
        this.lookupKey = _lookupKey;
        this.memberName = memberName;
        this.contactMode = contactMode;
        this.contactDetail = contactDetail;
        this.restrictionCount = _restrictionCount;
    }

    long memberId;
    String lookupKey;
    String memberName;
    String contactDetail;
    int contactMode;
    long restrictionCount;

    public long getRestrictionCount() {
        return restrictionCount;
    }

    public void setRestrictionCount(long count) {
        this.restrictionCount = count;
    }

    public long getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getContactDetail() {
        return contactDetail;
    }

    public int getContactMode() {
        return contactMode;
    }

    public void setContactMode(int contactMode) {
        this.contactMode = contactMode;
    }

    public String getLookupKey() {
        return lookupKey;
    }

    public void setLookupKey(String lookupKey) {
        this.lookupKey = lookupKey;
    }

    @Override
    public int compareTo(MemberRowDetails another) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.memberName, another.memberName);
    }
}
