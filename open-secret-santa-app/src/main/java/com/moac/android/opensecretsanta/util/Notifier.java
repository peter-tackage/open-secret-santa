package com.moac.android.opensecretsanta.util;

import com.moac.android.opensecretsanta.model.Member;

public interface Notifier {

    public void notify(Member _giver, String _receiverName, String _customMsg);
}
