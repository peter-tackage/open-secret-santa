package com.moac.android.opensecretsanta.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Group.TABLE_NAME)
public class Group extends PersistableObject {

    public static final String TABLE_NAME =  "groups";

    public static interface Columns extends PersistableObject.Columns {
        public static final String NAME_COLUMN = "NAME";
        public static final String MESSAGE_COLUMN = "MESSAGE";
        public static final String CREATED_AT_COLUMN = "CREATED_AT";
        public static final String DRAW_DATE_COLUMN = "DRAW_DATE";
    }

    @DatabaseField(columnName = Columns.NAME_COLUMN, unique = true, canBeNull = false)
    private String mName;

    @DatabaseField(columnName = Columns.MESSAGE_COLUMN)
    private String mMessage = "";

    @DatabaseField(columnName =  Columns.CREATED_AT_COLUMN)
    private long mCreatedAt;

    @DatabaseField(columnName = Columns.DRAW_DATE_COLUMN)
    private long mDrawDate = UNSET_DATE;

    @ForeignCollectionField(eager = false)
    private java.util.Collection<Member> mMembers;

    public String getName() { return mName; }
    public void setName(String _name) { mName = _name; }

    public long getCreatedAt() { return mCreatedAt; }
    public void setCreatedAt(long _createdAt) { mCreatedAt = _createdAt; }

    public long getDrawDate() { return mDrawDate; }
    public void setDrawDate(long _drawDate) { mDrawDate = _drawDate; }

    public void setMessage(String _message) { mMessage = _message; }
    public String getMessage() { return mMessage; }

}
