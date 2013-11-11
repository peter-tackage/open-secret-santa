package com.moac.android.opensecretsanta.model;

public enum ContactMode {

    REVEAL_ONLY("Reveal", false), SMS("SMS", true), EMAIL("Email", true);

    private final String mText;
    private final boolean mIsSendable;

    ContactMode(String _text, boolean _isSendable) {
        mText = _text;
        mIsSendable = _isSendable;
    }

    public String getText() { return mText; }
    public boolean isSendable() { return mIsSendable; }

}
