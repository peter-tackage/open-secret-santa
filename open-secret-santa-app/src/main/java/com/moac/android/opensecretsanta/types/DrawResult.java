package com.moac.android.opensecretsanta.types;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.HashSet;
import java.util.Set;

@DatabaseTable(tableName = DrawResult.TABLE_NAME)
public class DrawResult extends PersistableObject {

    public static final String TABLE_NAME =  "draw_results";

    public static final long UNDRAWN_DATE = -1;

    public static interface Columns extends PersistableObject.Columns {
        public static final String DRAW_DATE_COLUMN = "DRAW_DATE";
        public static final String GROUP_ID_COLUMN = "GROUP_ID";
    }

    @DatabaseField(columnName = Columns.DRAW_DATE_COLUMN)
    private long mDrawDate = UNDRAWN_DATE;

    // FIXME Doesn\t work. ORMLite issue - Only allow a single Draw Result per Group.
    @DatabaseField(columnName = Columns.GROUP_ID_COLUMN, foreign = true, canBeNull = false, unique = true,
    columnDefinition = "integer references groups (_id) on delete cascade")
    private Group mGroup;

    @ForeignCollectionField(eager = false)
    private java.util.Collection<DrawResultEntry> mDrawResultEntries;

    public long getDrawDate() { return mDrawDate; }
    public void setDrawDate(long _drawDate) { mDrawDate = _drawDate; }

    public void setGroup(Group _group) { mGroup = _group; }
    public long getGroupId() { return mGroup.getId(); }

}
