package com.moac.android.opensecretsanta.adapter;

import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Member;

public class MemberRowDetails implements Comparable<MemberRowDetails> {

    final protected Member mMember;
    final protected Assignment mAssignment;

    public MemberRowDetails(Member _member, Assignment _assignment) {
        mMember = _member;
        mAssignment = _assignment;
    }

    public Member getMember() { return mMember; }
    public Assignment getAssignment() { return mAssignment; }

    @Override
    public int compareTo(MemberRowDetails _that) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.mMember.getName(), _that.getMember().getName());
    }
}
