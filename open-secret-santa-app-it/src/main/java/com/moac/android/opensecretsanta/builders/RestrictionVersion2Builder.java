package com.moac.android.opensecretsanta.builders;

import com.moac.android.opensecretsanta.model.version2.MemberVersion2;
import com.moac.android.opensecretsanta.model.version2.RestrictionVersion2;

public class RestrictionVersion2Builder {

    public static String TEST_GIVER_NAME = "giver";
    public static String TEST_RECEIVER_NAME = "receiver";

    MemberVersion2 mGiver;
    MemberVersion2 mReceiver;

    public RestrictionVersion2Builder withGiver(MemberVersion2 giver) {
        mGiver = giver;
        return this;
    }

    public RestrictionVersion2Builder withReceiver(MemberVersion2 receiver) {
        mReceiver = receiver;
        return this;
    }

    public RestrictionVersion2 build() {
        RestrictionVersion2 restriction = new RestrictionVersion2();

        // build default required Member giver and receiver
        if (mGiver == null) {
            MemberVersion2Builder memberBuilder = new MemberVersion2Builder();
            mGiver = memberBuilder.withName(TEST_GIVER_NAME).build();
        }

        if (mReceiver == null) {
            MemberVersion2Builder memberBuilder = new MemberVersion2Builder();
            mReceiver = memberBuilder.withName(TEST_RECEIVER_NAME).build();
        }

        restriction.setMember(mGiver);
        restriction.setMember(mReceiver);
        return restriction;
    }
}
