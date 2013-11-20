package com.moac.android.opensecretsanta.util;

import com.moac.android.opensecretsanta.model.ContactMethod;

public class ContactDetailsValidator implements Validator {

    private final ContactMethod mContactMethod;
    private final String mDetails;

    public ContactDetailsValidator(ContactMethod _method, String _details) {
        mContactMethod = _method;
        mDetails = _details;
    }

    @Override
    public boolean isValid() {
        switch(mContactMethod) {
            case EMAIL:
                return new EmailValidator(mDetails).isValid();
            case SMS:
                return new SmsValidator(mDetails).isValid();
            case REVEAL_ONLY:
            default:
                return true;
        }
    }
}
