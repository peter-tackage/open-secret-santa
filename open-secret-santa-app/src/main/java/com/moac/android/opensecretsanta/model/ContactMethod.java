package com.moac.android.opensecretsanta.model;

public enum ContactMethod {

    REVEAL_ONLY("Reveal", false), SMS("SMS", true), EMAIL("Email", true);

    private final String mText;
    private final boolean mIsSendable;

    ContactMethod(String _text, boolean _isSendable) {
        mText = _text;
        mIsSendable = _isSendable;
    }

    public String getText() { return mText; }
    public boolean isSendable() { return mIsSendable; }

}
