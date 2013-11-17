package com.moac.android.opensecretsanta.builders;

import com.moac.android.opensecretsanta.model.version2.ConstantsVersion2;
import com.moac.android.opensecretsanta.model.version2.GroupVersion2;
import com.moac.android.opensecretsanta.model.version2.MemberVersion2;

public class MemberVersion2Builder {

    public static String TEST_MEMBER_NAME = "member";
    public static String TEST_MEMBER_CONTACT_DETAILS = "+1191191@";
    public static String TEST_MEMBER_LOOKUP_KEY = "AAABBB1111";

    String mLookupKey = TEST_MEMBER_LOOKUP_KEY;
    String mName = TEST_MEMBER_NAME;
    String mContactDetails = TEST_MEMBER_CONTACT_DETAILS;
    int mContactMode = ConstantsVersion2.NAME_ONLY_CONTACT_MODE;
    GroupVersion2 mGroup;

    public MemberVersion2Builder withLookupKey(String key) {
        mLookupKey = key;
        return this;
    }

    public MemberVersion2Builder withName(String name) {
        mName = name;
        return this;
    }

    public MemberVersion2Builder withContactDetails(String details) {
        mContactDetails = details;
        return this;
    }

    public MemberVersion2Builder withContactMode(int mode) {
        mContactMode = mode;
        return this;
    }

    public MemberVersion2Builder withGroup(GroupVersion2 group) {
        mGroup = group;
        return this;
    }


    public MemberVersion2 build() {
        MemberVersion2 member = new MemberVersion2();
        member.setLookupKey(mLookupKey);
        member.setName(mName);
        member.setContactDetail(mContactDetails);
        member.setContactMode(mContactMode);

        // build default required Group
        if (mGroup == null) {
            GroupVersion2Builder gBuilder = new GroupVersion2Builder();
            mGroup = gBuilder.build();
        }
        member.setGroup(mGroup);
        return member;
    }
}
