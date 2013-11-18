package com.moac.android.opensecretsanta.model.version2;

/**
 * Old DrawResult for database version2
 */

import android.provider.BaseColumns;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.moac.android.opensecretsanta.model.PersistableObject;

@DatabaseTable(tableName = DrawResultVersion2.TABLE_NAME)
public class DrawResultVersion2 extends PersistableObject {

    public static final String TABLE_NAME = "draw_results";

    @DatabaseField(columnName = Columns.DRAW_DATE_COLUMN)
    private long mDrawDate = ConstantsVersion2.UNDRAWN_DATE;

    @DatabaseField(columnName = Columns.SEND_DATE_COLUMN)
    private long mSendDate = ConstantsVersion2.UNSENT_DATE;

    @DatabaseField(columnName = Columns.MESSAGE_COLUMN)
    private String mMessage = "";

    @DatabaseField(columnName = Columns.GROUP_ID_COLUMN, foreign = true, canBeNull = false,
      columnDefinition = "integer references groups (_id) on delete cascade")
    private GroupVersion2 mGroup;

    @ForeignCollectionField(eager = false)
    private java.util.Collection<DrawResultEntryVersion2> mDrawResultEntries;

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

    public void setGroup(GroupVersion2 group) { mGroup = group; }

    public long getGroupId() { return mGroup.getId(); }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        DrawResultVersion2 drawResult = (DrawResultVersion2) obj;
        return (mDrawDate == (drawResult.getDrawDate()) &&
          mSendDate == (drawResult.getSendDate()) &&
          mMessage.equals(drawResult.getMessage()) &&
          (mGroup.getId() == (drawResult.getGroupId())));
    }
}