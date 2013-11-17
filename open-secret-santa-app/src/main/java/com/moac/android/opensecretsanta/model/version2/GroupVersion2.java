package com.moac.android.opensecretsanta.model.version2;

/**
 * Old Group for database version2
 */

import android.provider.BaseColumns;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.moac.android.opensecretsanta.model.PersistableObject;

@DatabaseTable(tableName = GroupVersion2.TABLE_NAME)
public class GroupVersion2 extends PersistableObject {

    public static final String TABLE_NAME =  "groups";

    // GROUP TABLE COLUMNS
    public static interface Columns extends BaseColumns {

        public static final String NAME_COLUMN = "NAME";
        public static final String IS_READY = "IS_READY";

        public static final String DEFAULT_SORT_ORDER = NAME_COLUMN + " ASC";

        public static final String[] ALL = {
                _ID,
                NAME_COLUMN,
                IS_READY
        };
    }


    @DatabaseField(columnName = Columns.NAME_COLUMN, unique = true, canBeNull = false)
    private String mName;

    @DatabaseField(columnName = Columns.IS_READY)
    private boolean mReady = false; // is the group ready to be notified.

    @ForeignCollectionField(eager = false)
    private java.util.Collection<MemberVersion2> mMembers;

    public String getName() { return mName; }
    public void setName(String name) { mName = name; }

    public void setReady(boolean ready) { mReady = ready; }
    public boolean isReady() { return mReady; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        GroupVersion2 group = (GroupVersion2) obj;
        return ((mName.equals(group.getName())) &&
                mReady == group.isReady());
    }


}