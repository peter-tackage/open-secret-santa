package com.moac.android.opensecretsanta.util.validator;

import android.util.Patterns;

public class SmsValidator implements Validator{

    private final String mPhoneNumber;
    private String mMsg;

    public SmsValidator(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    @Override
    public boolean isValid() {
        if(!Patterns.PHONE.matcher(mPhoneNumber).matches()) {
            mMsg = "Phone number isn't valid";
        }
        return mMsg == null;
    }

    @Override
    public String getMsg() {
       return mMsg;
    }
}
