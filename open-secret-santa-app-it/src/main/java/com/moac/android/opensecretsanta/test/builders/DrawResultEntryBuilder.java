package com.moac.android.opensecretsanta.test.builders;

import com.moac.android.opensecretsanta.types.DrawResultEntry;
import com.moac.android.opensecretsanta.types.Member;

public class DrawResultEntryBuilder {

    Member mGiver = new MemberBuilder().withName("Mr Giver").build();
    Member mReceiver = new MemberBuilder().withName("Mr Receiver").build();

    public DrawResultEntryBuilder withGiver(Member _giver) {
        mGiver = _giver;
        return this;
    }

    public DrawResultEntryBuilder withReceiver(Member _receiver) {
        mReceiver = _receiver;
        return this;
    }

    public DrawResultEntry build() {
        DrawResultEntry dre = new DrawResultEntry();
        dre.setGiverMember(mGiver);
        dre.setReceiverMember(mReceiver);
        return dre;
    }
}
