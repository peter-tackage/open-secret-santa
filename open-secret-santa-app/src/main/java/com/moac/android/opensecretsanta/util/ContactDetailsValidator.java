package com.moac.android.opensecretsanta.util;

import com.moac.android.opensecretsanta.model.ContactMethod;

public class ContactDetailsValidator implements Validator {

    private final ContactMethod mContactMethod;
    private final String mDetails;
    private String mMsg;

    public ContactDetailsValidator(ContactMethod _method, String _details) {
        mContactMethod = _method;
        mDetails = _details;
    }

    @Override
    public boolean isValid() {
        Validator val;
        switch(mContactMethod) {
            case EMAIL:
                val = new EmailValidator(mDetails);
                break;
            case SMS:
                val = new SmsValidator(mDetails);
                break;
            case REVEAL_ONLY:
            default:
                return true;
        }
        boolean isValid = val.isValid();
        mMsg = val.getMsg();
        return isValid;
    }

    @Override
    public String getMsg() {
        return mMsg;
    }
}
