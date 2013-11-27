package com.moac.android.opensecretsanta.notify;

import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Member;

public interface Notifier {
    public void notify(Assignment _assignment, Member _giver, String _receiverName, String _groupMsg);
}
