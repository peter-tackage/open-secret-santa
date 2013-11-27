package com.moac.android.opensecretsanta.draw;

public interface MemberEditor {
    // Requests to perform actions.
    public void onEditMember(long _memberId);
    public void onRestrictMember(long _groupId, long _memberId);
}
