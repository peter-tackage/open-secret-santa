package com.moac.android.opensecretsanta.util;

public class EmailValidator implements Validator {

    private final String mEmail;
    private String mMsg;

    public EmailValidator(String email) {
        mEmail = email;
    }

    @Override
    public boolean isValid() {
        // TODO Better/regex email validation
        if(mEmail == null || mEmail.isEmpty() || !mEmail.contains("@")) {
          mMsg = "Something doesn't seem right!";
        }
        return mMsg == null;
    }

    @Override
    public String getMsg() {
        return mMsg;
    }
}
