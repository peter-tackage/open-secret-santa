package com.moac.android.opensecretsanta.types;

import android.provider.BaseColumns;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.moac.android.opensecretsanta.activity.Constants;
import com.moac.android.opensecretsanta.database.OpenSecretSantaDB;

@DatabaseTable(tableName = OpenSecretSantaDB.DRAW_RESULTS_TABLE_NAME)
public class DrawResult extends PersistableObject {

    @DatabaseField(columnName = Columns.DRAW_DATE_COLUMN)
    private long mDrawDate = Constants.UNDRAWN_DATE;

    @DatabaseField(columnName = Columns.SEND_DATE_COLUMN)
    private long mSendDate = Constants.UNSENT_DATE;

    @DatabaseField(columnName = Columns.MESSAGE_COLUMN)
    private String mMessage = "";

    @DatabaseField(columnName = Columns.GROUP_ID_COLUMN, foreign = true, canBeNull = false,
      columnDefinition = "integer references groups (_id) on delete cascade")
    private Group mGroup;

    @ForeignCollectionField(eager = false)
    private java.util.Collection<DrawResultEntry> mDrawResultEntries;

    public static interface Columns extends BaseColumns {

        // Results definition
        public static final String DRAW_DATE_COLUMN = "DRAW_DATE";
        public static final String SEND_DATE_COLUMN = "SEND_DATE";
        public static final String MESSAGE_COLUMN = "MESSAGE";
        public static final String GROUP_ID_COLUMN = "GROUP_ID";

        public static final String DEFAULT_SORT_ORDER = DRAW_DATE_COLUMN + " DESC";

        public static String[] ALL = { _ID, DRAW_DATE_COLUMN, SEND_DATE_COLUMN, MESSAGE_COLUMN, GROUP_ID_COLUMN };
    }

    public long getDrawDate() { return mDrawDate; }
    public void setDrawDate(long _drawDate) { mDrawDate = _drawDate; }
    public void setSendDate(long sendDate) { mSendDate = sendDate; }
    public long getSendDate() { return mSendDate; }
    public void setMessage(String message) { mMessage = message; }
    public String getMessage() { return mMessage; }
    public void setGroup(Group group) { mGroup = group; }
    public long getGroupId() { return mGroup.getId(); }

}
