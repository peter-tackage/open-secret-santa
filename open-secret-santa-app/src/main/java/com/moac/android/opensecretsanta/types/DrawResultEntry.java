package com.moac.android.opensecretsanta.types;

import android.provider.BaseColumns;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.moac.android.opensecretsanta.activity.Constants;

@DatabaseTable(tableName = DrawResultEntry.TABLE_NAME)
public class DrawResultEntry extends PersistableObject {

    public static final String TABLE_NAME =  "draw_result_entries";

    public static final long UNVIEWED_DATE = -1;
    public static final long UNSENT_DATE = -1;

    public static interface Columns extends BaseColumns {
        public static final String DRAW_RESULT_ID_COLUMN = "DRAW_RESULT_ID";
        public static final String GIVER_MEMBER_ID_COLUMN = "GIVER_MEMBER_ID";
        public static final String RECEIVER_MEMBER_ID_COLUMN = "RECEIVER_MEMBER_ID";
        public static final String VIEWED_DATE_COLUMN = "VIEWED_DATE";
        public static final String SENT_DATE_COLUMN = "SENT_DATE";
        public static final String SEND_STATUS_COLUMN = "SEND_STATUS";
    }

    /**
     * Unique combo with mGiver and mDrawResult only prevents a giver having multiple recipients,
     * rather than a recipient having multiple givers. I don't think there's a way to support
     * two separate combos in ORMLite - eg mGiver/mDrawResult & mReceiver/mDrawResult.
     *
     * Perhaps this is an argument for is associating the DRE with the Members only and
     * not also the DrawResult (which perhaps could be discarded)
     */

    @DatabaseField(columnName = Columns.GIVER_MEMBER_ID_COLUMN, foreign = true, canBeNull = false, uniqueCombo = true,
      columnDefinition = "integer references members (_id) on delete cascade")
    private Member mGiver;

    @DatabaseField(columnName = Columns.RECEIVER_MEMBER_ID_COLUMN, foreign = true, canBeNull = false,
      columnDefinition = "integer references members (_id) on delete cascade")
    private Member mReceiver;

    @DatabaseField(columnName = Columns.VIEWED_DATE_COLUMN)
    private long mViewedDate = UNVIEWED_DATE;

    @DatabaseField(columnName = Columns.SENT_DATE_COLUMN)
    private long mSentDate = UNSENT_DATE;

    @DatabaseField(columnName = Columns.SEND_STATUS_COLUMN)
    private long mSendStatus;

    @DatabaseField(columnName = Columns.DRAW_RESULT_ID_COLUMN, foreign = true, canBeNull = false, uniqueCombo = true,
      columnDefinition = "integer references draw_results (_id) on delete cascade")
    private DrawResult mDrawResult;


    public long getDrawResultId() { return mDrawResult.getId(); }
    public void setDrawResult(DrawResult _drawResult) { mDrawResult = _drawResult; }

    public long getGiverMemberId() { return mGiver.getId(); }
    public void setGiverMember(Member _giver) { mGiver = _giver; }

    public long getReceiverMemberId() { return mReceiver.getId(); }
    public void setReceiverMember(Member _receiver) { mReceiver = _receiver; }

    public long getViewedDate() { return mViewedDate; }
    public void setViewedDate(long _viewedDate) { mViewedDate = _viewedDate; }

    public long getSentDate() { return mSentDate; }
    public void setSentDate(long _sentDate) { mSentDate = _sentDate; }

    public long getSendStatus() { return mSendStatus; }
    public void setSendStatus(long _sendStatus) { mSendStatus = _sendStatus; }

}
