package com.moac.android.opensecretsanta.notify;

import com.moac.android.opensecretsanta.model.Group;

public interface DrawNotifier {
    public void notifyDraw(NotifyAuthorization auth, Group group, long[] memberIds);
}
