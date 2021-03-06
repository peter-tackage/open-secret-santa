package com.moac.android.opensecretsanta.notify.mail;

import com.moac.android.opensecretsanta.notify.NotificationFailureException;

public interface EmailTransporter {
    public void send(String subject, String body, String fromAddress,
                     String toAddress, String oauthToken) throws NotificationFailureException;
}
