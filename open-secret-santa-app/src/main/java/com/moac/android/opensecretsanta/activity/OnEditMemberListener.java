package com.moac.android.opensecretsanta.activity;

public interface OnEditMemberListener {

    public void onEditMember(long _groupId, long _memberId);
    public void onRestrictMember(long _groupId, long _memberId);

}
