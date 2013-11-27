package com.moac.android.opensecretsanta.model;

public enum ContactMethod {

    REVEAL_ONLY("Reveal Only", false), SMS("SMS", true), EMAIL("Email", true);

    private final String mDisplayText;
    private final boolean mIsSendable;

    ContactMethod(String _text, boolean _isSendable) {
        mDisplayText = _text;
        mIsSendable = _isSendable;
    }

    public String getDisplayText() { return mDisplayText; }
    public boolean isSendable() { return mIsSendable; }

    // #################################################################
    // Do not ever... EVER override ContactMethod's toString()
    // It is used by ORMLite, so if you change it, you will invalidate
    // this classes ability to map existing data. Very bad.
    // #################################################################
}
