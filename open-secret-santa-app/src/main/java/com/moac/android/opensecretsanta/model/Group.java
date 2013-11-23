package com.moac.android.opensecretsanta.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;

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
    private Collection<Member> mMembers;

    public String getName() { return mName; }
    public void setName(String _name) { mName = _name; }

    public long getCreatedAt() { return mCreatedAt; }
    public void setCreatedAt(long _createdAt) { mCreatedAt = _createdAt; }

    public long getDrawDate() { return mDrawDate; }
    public void setDrawDate(long _drawDate) { mDrawDate = _drawDate; }

    public void setMessage(String _message) { mMessage = _message; }
    public String getMessage() { return mMessage; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Group group = (Group) obj;
        // no need to check created at date as that's automatic on object creation which will differ
        return (mDrawDate == (group.getDrawDate()) &&
                mMessage.equals(group.getMessage()) &&
                mName.equals(group.getName()));
    }

    public static class GroupBuilder {
        Group mGroup;

        public GroupBuilder() {
            mGroup = new Group();
            // set the required field
            mGroup.setName("defaultRequiredGroupName");
        }


        // be careful this does not set the group id
        // if you insert a group created with this builder specifyin id x,
        // this will not necessarily be the group id after the insert...
        // so this withGroupId should only be used to set a known group id
        // for instance for setting a Member group property
        public GroupBuilder withGroupId(long _id) {
            mGroup.setId(_id);
            return this;
        }

        public GroupBuilder withName(String name) {
            mGroup.setName(name);
            return this;
        }

        public GroupBuilder withMessage(String message) {
            mGroup.setMessage(message);
            return this;
        }

        public GroupBuilder withDrawDate(long drawDate) {
            mGroup.setDrawDate(drawDate);
            return this;
        }

        public GroupBuilder withCreatedDate(long createdDate) {
            mGroup.setCreatedAt(createdDate);
            return this;
        }

        public Group build() { return mGroup; }
    }

}
