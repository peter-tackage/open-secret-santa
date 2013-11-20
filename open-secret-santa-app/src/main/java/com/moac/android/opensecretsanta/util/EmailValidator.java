package com.moac.android.opensecretsanta.util;

public class EmailValidator implements Validator {

    private final String mEmail;

    public EmailValidator(String email) {
        mEmail = email;
    }

    @Override
    public boolean isValid() {
        // TODO Better/regex email validation
        return mEmail != null && !mEmail.isEmpty() && mEmail.contains("@");
    }
}
