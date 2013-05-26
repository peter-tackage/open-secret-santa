package com.moac.android.opensecretsanta.test.builders;

import com.moac.android.opensecretsanta.activity.Constants;
import com.moac.android.opensecretsanta.types.Member;

public class MemberBuilder {

    private long id = -1;
    private String name = "member1";
    private String detail = "+1191191";
    private int mode = Constants.SMS_CONTACT_MODE;
    private String lookupKey = "AAABBB1111";

    public MemberBuilder withId(long id) {
        this.id = id;
        return this;
    }

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
        return new Member(this.id, this.name, this.lookupKey, this.detail, this.mode);
    }
}
