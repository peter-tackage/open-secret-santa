package com.moac.android.opensecretsanta.draw;

import com.moac.android.opensecretsanta.model.Group;

public interface MemberEditor {
    // Requests to perform actions.
    public void onEditMember(long _groupId, long _memberId);
    public void onRestrictMember(long _groupId, long _memberId);
    public void onNotifyDraw(Group _group, long[] _memberIds);
    public void onNotifyDraw(Group _group);
}
