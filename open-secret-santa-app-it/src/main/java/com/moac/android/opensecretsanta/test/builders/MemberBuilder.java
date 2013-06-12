package com.moac.android.opensecretsanta.test.builders;

import com.moac.android.opensecretsanta.activity.ContactModes;
import com.moac.android.opensecretsanta.types.Member;

public class MemberBuilder {

    private String name = "member1";
    private String detail = "+1191191";
    private int mode = ContactModes.SMS_CONTACT_MODE;
    private String lookupKey = "AAABBB1111";


    public MemberBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public MemberBuilder withContactDetail(String detail) {
        this.detail = detail;
        return this;
    }

    public MemberBuilder withContactMode(int mode) {
        this.mode = mode;
        return this;
    }

    public MemberBuilder withLookupKey(String key) {
        this.lookupKey = key;
        return this;
    }

    public Member build() {
        Member member  = new Member();
        member.setName(name);
        member.setLookupKey(lookupKey);
        member.setContactDetail(detail);
        member.setContactMode(mode);
        return member;
    }
}
