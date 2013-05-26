package com.moac.android.opensecretsanta.activity;

public class RestrictionRowDetails implements Comparable<RestrictionRowDetails> {

    public RestrictionRowDetails(long fromMemberId, long toMemberId, String toMemberName, boolean restricted) {
        this.fromMemberId = fromMemberId;
        this.toMemberId = toMemberId;
        this.toMemberName = toMemberName;
        this.restricted = restricted;
    }

    long fromMemberId;
    long toMemberId;
    String toMemberName;
    boolean restricted;

    public boolean isRestricted() {
        return restricted;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    public long getToMemberId() {
        return toMemberId;
    }

    public String getToMemberName() {
        return toMemberName;
    }

    @Override
    public int compareTo(RestrictionRowDetails another) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.toMemberName, another.toMemberName);
    }
}