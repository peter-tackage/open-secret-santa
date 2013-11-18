package com.moac.android.opensecretsanta.notify;

import com.moac.android.opensecretsanta.model.Group;
import rx.Observable;

public interface NotifyExecutor {
    public Observable<NotifyStatusEvent> notifyDraw(Group group, long[] membersIds);
}
