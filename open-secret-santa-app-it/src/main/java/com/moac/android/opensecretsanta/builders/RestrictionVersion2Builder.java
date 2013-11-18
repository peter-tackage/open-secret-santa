package com.moac.android.opensecretsanta.builders;

import com.moac.android.opensecretsanta.model.version2.MemberVersion2;
import com.moac.android.opensecretsanta.model.version2.RestrictionVersion2;

public class RestrictionVersion2Builder {

    public static String TEST_MEMBER_NAME = "member";
    public static String OTHER_MEMBER_NAME = "other_member";

    MemberVersion2 mMember;
    MemberVersion2 mOtherMember;

    public RestrictionVersion2Builder withMember(MemberVersion2 giver) {
        mMember = giver;
        return this;
    }

    public RestrictionVersion2Builder withOtherMember(MemberVersion2 receiver) {
        mOtherMember = receiver;
        return this;
    }

    public RestrictionVersion2 build() {
        RestrictionVersion2 restriction = new RestrictionVersion2();

        // build default required Member giver and receiver
        if (mMember == null) {
            MemberVersion2Builder memberBuilder = new MemberVersion2Builder();
            mMember = memberBuilder.withName(TEST_MEMBER_NAME).build();
        }

        if (mOtherMember == null) {
            MemberVersion2Builder memberBuilder = new MemberVersion2Builder();
            mOtherMember = memberBuilder.withName(OTHER_MEMBER_NAME).build();
        }

        restriction.setMember(mMember);
        restriction.setOtherMember(mOtherMember);
        return restriction;
    }
}
