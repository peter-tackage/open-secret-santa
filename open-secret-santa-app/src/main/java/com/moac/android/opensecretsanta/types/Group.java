package com.moac.android.opensecretsanta.types;

import android.provider.BaseColumns;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.moac.android.opensecretsanta.database.OpenSecretSantaDB;

@DatabaseTable(tableName = OpenSecretSantaDB.GROUPS_TABLE_NAME)
public class Group extends PersistableObject {

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

    public String getName() { return mName; }
    public void setName(String name) { mName = name; }
    public void setReady(boolean ready) { mReady = ready; }
    public boolean isReady() { return mReady; }

}
