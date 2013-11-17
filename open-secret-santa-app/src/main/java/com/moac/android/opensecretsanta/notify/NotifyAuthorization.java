package com.moac.android.opensecretsanta.notify;

public class NotifyAuthorization {

    private EmailAuthorization mEmailAuth;

    void setEmailAuthorization(EmailAuthorization emailAuth) {
        mEmailAuth = emailAuth;
    }

    EmailAuthorization getEmailAuth() {
        return mEmailAuth;
    }

    public static class Builder {
        private NotifyAuthorization auth;

        public Builder() {
            auth = new NotifyAuthorization();
        }

        public Builder withEmailAuthorization(EmailAuthorization emailAuth) {
            auth.mEmailAuth = emailAuth;
            return this;
        }

        public NotifyAuthorization build() {
            return auth;
        }
    }
}
