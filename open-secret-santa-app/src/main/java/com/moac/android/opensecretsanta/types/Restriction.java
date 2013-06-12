package com.moac.android.opensecretsanta.types;

import android.provider.BaseColumns;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Restriction.TABLE_NAME)
public class Restriction extends PersistableObject {

    public static final String TABLE_NAME = "restrictions";

    public static interface Columns extends BaseColumns {
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
}
