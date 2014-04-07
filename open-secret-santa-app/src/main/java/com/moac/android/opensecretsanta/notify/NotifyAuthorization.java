package com.moac.android.opensecretsanta.notify;

import com.moac.android.opensecretsanta.notify.mail.EmailAuthorization;

public class NotifyAuthorization {

    private EmailAuthorization mEmailAuth;

    EmailAuthorization getEmailAuth() {
        return mEmailAuth;
    }

    public static class Builder {
        private NotifyAuthorization auth;

        public Builder() {
            auth = new NotifyAuthorization();
        }

        public Builder withAuth(EmailAuthorization emailAuth) {
            auth.mEmailAuth = emailAuth;
            return this;
        }

        public NotifyAuthorization build() {
            return auth;
        }
    }
}
