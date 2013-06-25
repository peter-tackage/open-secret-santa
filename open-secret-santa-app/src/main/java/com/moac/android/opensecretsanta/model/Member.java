package com.moac.android.opensecretsanta.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.moac.android.opensecretsanta.activity.ContactModes;

@DatabaseTable(tableName = Member.TABLE_NAME)
public final class Member extends PersistableObject {

    public static final String TABLE_NAME =  "members";

    public static interface Columns extends PersistableObject.Columns {
        public static final String LOOKUP_KEY = "LOOKUP_KEY";
        public static final String CONTACT_ID = "CONTACT_ID";
        public static final String NAME_COLUMN = "NAME";
        public static final String CONTACT_MODE_COLUMN = "CONTACT_MODE";
        public static final String CONTACT_ADDRESS_COLUMN = "CONTACT_ADDRESS";
        public static final String GROUP_ID_COLUMN = "GROUP_ID";
    }

    @DatabaseField(columnName = Columns.LOOKUP_KEY)
    private String mLookupKey;

    @DatabaseField(columnName = Columns.CONTACT_ID)
    private long mContactId = UNSET_ID;

    // The participant name
    @DatabaseField(columnName = Columns.NAME_COLUMN, canBeNull = false, uniqueCombo = true)
    private String mName;

    // the email, the  phone number, the whatever.
    @DatabaseField(columnName = Columns.CONTACT_ADDRESS_COLUMN)
    private String mContactAddress;

    // The model of communication to be used.
    @DatabaseField(columnName = Columns.CONTACT_MODE_COLUMN, canBeNull = false)
    private int mContactMode = ContactModes.NAME_ONLY_CONTACT_MODE;

    @DatabaseField(columnName = Columns.GROUP_ID_COLUMN, foreign = true, canBeNull = false, uniqueCombo = true,
      columnDefinition = "integer references groups (_id) on delete cascade")
    private Group mGroup;

    @ForeignCollectionField(eager = false)
    private java.util.Collection<Restriction> mRestrictions;

    public String getName() { return mName; }
    public void setName(String _name) { mName = _name; }

    public String getContactAddress() { return mContactAddress; }
    public void setContactAddress(String _contactAddress) { mContactAddress = _contactAddress; }

    public int getContactMode() { return mContactMode; }
    public void setContactMode(int _contactMode) { mContactMode = _contactMode; }

    public String getLookupKey() { return mLookupKey; }
    public void setLookupKey(String _lookupKey) { mLookupKey = _lookupKey; }

    public void setGroup(Group _group) { mGroup = _group; }
    public long getGroupId() { return mGroup.getId(); }

    public long getContactId() { return mContactId; }
    public void setContactId(long contactId) { mContactId = contactId; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Id: ");
        sb.append(getId());
        sb.append(", Name: ");
        sb.append(getName());
        sb.append(", Contact Address: ");
        sb.append(getContactAddress());
        sb.append(", Contact Mode: ");
        sb.append(getContactMode());
        sb.append(", Lookup Key: ");
        sb.append(getLookupKey());

        return sb.toString();
    }

//    @Override
//    public boolean equals(Object other) {
//        if(this == other) return true;
//        if(!(other instanceof Member)) return false;
//
//        Member that = (Member) other;
//        return
//          (null == this.mContactAddress ? (this.mContactAddress == that.mContactAddress) : this.mContactAddress.equals(that.mContactAddress))
//            &&
//            (null == this.mLookupKey ? (this.mLookupKey == that.mLookupKey) : this.mLookupKey.equals(that.mLookupKey))
//            &&
//            (null == this.mName ? (this.mName == that.mName) : this.mName.equals(that.mName))
//            &&
//            this.mContactMode == that.mContactMode;
//    }

}
