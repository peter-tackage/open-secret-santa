package com.moac.android.opensecretsanta.notify.sms;

import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Member;

public interface SmsTransporter {
    public void send(Assignment _assignment, Member _giver, String _receiverName, String _groupMsg);
}
