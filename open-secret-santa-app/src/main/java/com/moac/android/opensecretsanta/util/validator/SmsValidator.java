package com.moac.android.opensecretsanta.util.validator;

public class SmsValidator implements Validator{

    private final String mPhoneNumber;
    private String mMsg;

    public SmsValidator(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    @Override
    public boolean isValid() {
        // TODO A bit like email in terms of actual validation.
        if(mPhoneNumber == null || mPhoneNumber.isEmpty()) {
            mMsg = "Something doesn't seem right!";
        }
        return mMsg == null;
    }

    @Override
    public String getMsg() {
       return mMsg;
    }
}
