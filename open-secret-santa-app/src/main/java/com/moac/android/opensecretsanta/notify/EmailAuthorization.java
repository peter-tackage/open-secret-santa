package com.moac.android.opensecretsanta.notify;

public class EmailAuthorization {

    String mEmailAddress;
    String mToken;

    public EmailAuthorization(String emailAddress, String token) {
        this.mEmailAddress = emailAddress;
        this.mToken = token;
    }

    public String getEmailAddress() {
        return mEmailAddress;
    }

    public String getToken() {
        return mToken;
    }
}
