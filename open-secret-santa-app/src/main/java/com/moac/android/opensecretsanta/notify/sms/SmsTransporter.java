package com.moac.android.opensecretsanta.notify.sms;

import com.moac.android.opensecretsanta.model.Assignment;

public interface SmsTransporter {
    public void send(Assignment _assignment, String phoneNumber, String msg);
}
