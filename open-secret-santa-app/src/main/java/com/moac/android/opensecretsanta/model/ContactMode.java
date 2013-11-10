package com.moac.android.opensecretsanta.model;

public enum ContactMode {

    REVEAL_ONLY("Reveal"), SMS("SMS"), EMAIL("Email");

    private final String mText;

    ContactMode(String _text) {
        mText = _text;
    }

    public String getText() { return mText; }

}
