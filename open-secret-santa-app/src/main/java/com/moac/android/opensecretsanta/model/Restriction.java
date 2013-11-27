package com.moac.android.opensecretsanta.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Restriction.TABLE_NAME)
public class Restriction extends PersistableObject {

    public static final String TABLE_NAME = "restrictions";

    public static interface Columns extends PersistableObject.Columns {
        public static final String MEMBER_ID_COLUMN = "MEMBER_ID";
        public static final String OTHER_MEMBER_ID_COLUMN = "OTHER_MEMBER_ID";
    }

    @DatabaseField(columnName = Columns.MEMBER_ID_COLUMN, foreign = true, canBeNull = false,
      columnDefinition = "integer references members (_id) on delete cascade")
    private Member mMember;

    @DatabaseField(columnName = Columns.OTHER_MEMBER_ID_COLUMN, foreign = true, canBeNull = false,
      columnDefinition = "integer references members (_id) on delete cascade")
    private Member mOtherMember;

    public long getMemberId() { return mMember.getId(); }
    public void setMember(Member _member) { mMember = _member; }

    public long getOtherMemberId() { return mOtherMember.getId(); }
    public void setOtherMember(Member _otherMember) { mOtherMember = _otherMember; }
    public void setOtherMemberId(long id) {
        Member otherMember = new Member();
        otherMember.setId(id);
        mOtherMember = otherMember;
    }

    public static class Builder {
        Restriction mRestriction;
        public Builder() {
            mRestriction = new Restriction();
            // set the required fields. this doesn't really make sense but all we need is a member
            Member.MemberBuilder memberBuilder = new Member.MemberBuilder();
            mRestriction.setMember(memberBuilder.build());
            mRestriction.setOtherMember(memberBuilder.build());
        }
        public Builder withMember(Member member) {
            mRestriction.setMember(member);
            return this;
        }
        public Builder withOtherMember(Member member) {
            mRestriction.setOtherMember(member);
            return this;
        }
        public Restriction build() { return mRestriction; }
    }
}
