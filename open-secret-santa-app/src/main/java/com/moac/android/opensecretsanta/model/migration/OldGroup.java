package com.moac.android.opensecretsanta.model.migration;

/**
 * Created with IntelliJ IDEA.
 * User: amelysh
 * Date: 13.11.13
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */

import android.provider.BaseColumns;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.moac.android.opensecretsanta.model.PersistableObject;

@DatabaseTable(tableName = "groups")
public class OldGroup extends PersistableObject {

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

    @ForeignCollectionField(eager = false)
    private java.util.Collection<OldMember> mMembers;


    public String getName() { return mName; }
    public void setName(String name) { mName = name; }


}