package com.moac.android.opensecretsanta.test.builders;

import com.moac.android.opensecretsanta.types.DrawResultEntry;

public class DrawResultEntryBuilder {

    String mGiverName = "Mr Giver";
    String mReceiverName = "Mr Receiver";

    public DrawResultEntryBuilder withGiverName(String giverName) {
        mGiverName = giverName;
        return this;
    }

    public DrawResultEntryBuilder withReceiverName(String receiverName) {
        mReceiverName = receiverName;
        return this;
    }

    public DrawResultEntry build() {
        DrawResultEntry dre = new DrawResultEntry();
        dre.setGiverName(mGiverName);
        dre.setReceiverName(mReceiverName);
        return dre;
    }
}
