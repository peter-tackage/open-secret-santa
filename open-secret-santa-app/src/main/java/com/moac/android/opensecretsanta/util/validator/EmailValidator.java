package com.moac.android.opensecretsanta.util.validator;

import android.util.Patterns;

public class EmailValidator implements Validator {

    private final String mEmail;
    private String mMsg;

    public EmailValidator(String email) {
        mEmail = email;
    }

    @Override
    public boolean isValid() {
        if(!Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()) {
          mMsg = "Email address isn't valid";
        }
        return mMsg == null;
    }

    @Override
    public String getMsg() {
        return mMsg;
    }
}
