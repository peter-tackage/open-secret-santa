package com.moac.android.opensecretsanta.adapter;

import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Member;

public class MemberRowDetails {

    protected Member mMember;
    protected Assignment mAssignment;

    public MemberRowDetails(Member _member, Assignment _assignment) {
        mMember = _member;
        mAssignment = _assignment;
    }

    public Member getMember() { return mMember; }
    public Assignment getAssignment() { return mAssignment; }

}
