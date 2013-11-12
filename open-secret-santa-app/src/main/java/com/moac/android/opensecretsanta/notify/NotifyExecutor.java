package com.moac.android.opensecretsanta.notify;

import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;

import java.util.List;

public interface NotifyExecutor {
    public void notifyDraw(Group group, long[] membersIds);
}
