package com.moac.android.opensecretsanta.model.migration;

/**
 * Created with IntelliJ IDEA.
 * User: amelysh
 * Date: 13.11.13
 * Time: 23:14
 * To change this template use File | Settings | File Templates.
 */

import android.provider.BaseColumns;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.moac.android.opensecretsanta.model.PersistableObject;

@DatabaseTable(tableName = "draw_results")
public class OldDrawResult extends PersistableObject {

    @DatabaseField(columnName = Columns.DRAW_DATE_COLUMN)
    private long mDrawDate = OldConstants.UNDRAWN_DATE;

    @DatabaseField(columnName = Columns.SEND_DATE_COLUMN)
    private long mSendDate = OldConstants.UNSENT_DATE;

    @DatabaseField(columnName = Columns.MESSAGE_COLUMN)
    private String mMessage = "";

    @DatabaseField(columnName = Columns.GROUP_ID_COLUMN, foreign = true, canBeNull = false,
            columnDefinition = "integer references groups (_id) on delete cascade")
    private OldGroup mGroup;


    @ForeignCollectionField(eager = false)
    private java.util.Collection<OldDrawResultEntry> mDrawResultEntries;

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
    public void setGroup(OldGroup group) { mGroup = group; }
    public long getGroupId() { return mGroup.getId(); }

}