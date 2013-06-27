package com.moac.android.opensecretsanta.activity;

import com.moac.android.opensecretsanta.model.DrawResult;
import com.moac.android.opensecretsanta.model.Group;

public interface DrawManager {

    public void onEditMember(long _groupId, long _memberId);
    public void onRestrictMember(long _groupId, long _memberId);
    public void onRequestDraw(Group _group);
    public void onNotifyDraw(DrawResult _result);

}
