package com.moac.android.opensecretsanta.builders;

import com.moac.android.opensecretsanta.model.ContactMethod;
import com.moac.android.opensecretsanta.model.Member;

public class MemberBuilder {

    private String name = "member1";
    private String contactDetails = "+1191191";
    private ContactMethod method = ContactMethod.SMS;
    private String lookupKey = "AAABBB1111";


    public MemberBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public MemberBuilder withContactDetails(String address) {
        this.contactDetails = address;
        return this;
    }

    public MemberBuilder withContactMethod(ContactMethod mode) {
        this.method = mode;
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
        member.setContactDetails(contactDetails);
        member.setContactMethod(method);
        return member;
    }
}
