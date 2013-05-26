package com.moac.android.opensecretsanta.types;

import android.provider.BaseColumns;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.moac.android.opensecretsanta.database.OpenSecretSantaDB;

@DatabaseTable(tableName = OpenSecretSantaDB.RESTRICTIONS_TABLE_NAME)
public class Restriction {

    public static interface Columns extends BaseColumns {

        public static final String MEMBER_ID_COLUMN = "MEMBER_ID";
        public static final String OTHER_MEMBER_ID_COLUMN = "OTHER_MEMBER_ID";

        public static final String[] ALL = {
          _ID,
          MEMBER_ID_COLUMN,
          OTHER_MEMBER_ID_COLUMN
        };

        public static final String DEFAULT_SORT_ORDER = OTHER_MEMBER_ID_COLUMN + " DESC";
    }

    @DatabaseField(columnName = Columns.MEMBER_ID_COLUMN)
    private long mMemberId = PersistableObject.UNSET_ID;

    @DatabaseField(columnName = Columns.OTHER_MEMBER_ID_COLUMN)
    private long mOtherMemberId = PersistableObject.UNSET_ID;

    public long getMemberId() {
        return mMemberId;
    }

    public long getOtherMemberId() {
        return mOtherMemberId;
    }

    public void setMemberId(long memberId) {
        mMemberId = memberId;
    }

    public void setOtherMemberId(long otherMemberId) {
        mOtherMemberId = otherMemberId;
    }
}
