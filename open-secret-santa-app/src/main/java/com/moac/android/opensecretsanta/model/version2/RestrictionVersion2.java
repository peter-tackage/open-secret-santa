package com.moac.android.opensecretsanta.model.version2;

/**
 * Old Restriction for database version2
 */
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.moac.android.opensecretsanta.model.PersistableObject;

@DatabaseTable(tableName = RestrictionVersion2.TABLE_NAME)
public class RestrictionVersion2 extends PersistableObject {

    public static final String TABLE_NAME = "restrictions";

    public static interface Columns extends PersistableObject.Columns {
        public static final String MEMBER_ID_COLUMN = "MEMBER_ID";
        public static final String OTHER_MEMBER_ID_COLUMN = "OTHER_MEMBER_ID";
    }

    @DatabaseField(columnName = Columns.MEMBER_ID_COLUMN, foreign = true, canBeNull = false,
            columnDefinition = "integer references members (_id) on delete cascade")
    private MemberVersion2 mMember;

    @DatabaseField(columnName = Columns.OTHER_MEMBER_ID_COLUMN, foreign = true, canBeNull = false,
            columnDefinition = "integer references members (_id) on delete cascade")
    private MemberVersion2 mOtherMember;

    public long getMemberId() { return mMember.getId(); }
    public void setMember(MemberVersion2 _member) { mMember = _member; }

    public long getOtherMemberId() { return mOtherMember.getId(); }
    public void setOtherMember(MemberVersion2 _otherMember) { mOtherMember = _otherMember; }

}