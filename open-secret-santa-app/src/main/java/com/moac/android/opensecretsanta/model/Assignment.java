package com.moac.android.opensecretsanta.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Assignment.TABLE_NAME)
public class Assignment extends PersistableObject {

    public enum Status {
        Assigned("Assigned"), Revealed("Revealed"), Sent("Sent"), Failed("Failed");

        private String mText;
        Status(String _text) {  mText = _text; }
        public String getText() { return mText; }
    }

    public static final String TABLE_NAME = "assignments";

    public static interface Columns extends PersistableObject.Columns {
        public static final String GIVER_MEMBER_ID_COLUMN = "GIVER_MEMBER_ID";
        public static final String RECEIVER_MEMBER_ID_COLUMN = "RECEIVER_MEMBER_ID";
        public static final String SEND_STATUS_COLUMN = "SEND_STATUS";
    }

    @DatabaseField(columnName = Columns.GIVER_MEMBER_ID_COLUMN, foreign = true, canBeNull = false,
      columnDefinition = "integer references members (_id) on delete cascade")
    private Member mGiver;

    @DatabaseField(columnName = Columns.RECEIVER_MEMBER_ID_COLUMN, foreign = true, canBeNull = false,
      columnDefinition = "integer references members (_id) on delete cascade")
    private Member mReceiver;

    @DatabaseField(columnName = Columns.SEND_STATUS_COLUMN)
    private Status mSendStatus = Status.Assigned;

    public long getGiverMemberId() { return mGiver.getId(); }
    public void setGiverMember(Member _giver) { mGiver = _giver; }

    public long getReceiverMemberId() { return mReceiver.getId(); }
    public void setReceiverMember(Member _receiver) { mReceiver = _receiver; }

    public Status getSendStatus() { return mSendStatus; }
    public void setSendStatus(Status _sendStatus) { mSendStatus = _sendStatus; }

    @Override
    public String toString() {
        return "Giver: " + getGiverMemberId() + " Receiver: " + getReceiverMemberId() + " Status: " + mSendStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Assignment assignment = (Assignment) obj;
        return (mGiver.getId() == assignment.getGiverMemberId() &&
                mReceiver.getId() == assignment.getReceiverMemberId() &&
                mSendStatus == assignment.getSendStatus());
    }
}
