package com.moac.android.opensecretsanta.util;


public class SmsValidator implements Validator{

    private final String mPhoneNumber;

    public SmsValidator(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    @Override
    public boolean isValid() {
        // TODO A bit like email in terms of actual validation.
        return mPhoneNumber != null && !mPhoneNumber.isEmpty();
    }
}
