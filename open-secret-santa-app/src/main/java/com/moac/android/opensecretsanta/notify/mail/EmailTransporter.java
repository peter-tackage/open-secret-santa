package com.moac.android.opensecretsanta.notify.mail;

import javax.mail.MessagingException;

public interface EmailTransporter {
    public void send(String subject, String body, String user,
                     String oauthToken, String recipients) throws MessagingException;
}
