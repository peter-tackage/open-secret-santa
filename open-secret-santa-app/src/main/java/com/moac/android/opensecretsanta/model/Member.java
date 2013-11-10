package com.moac.android.opensecretsanta.model;

import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Member.TABLE_NAME)
public final class Member extends PersistableObject  {

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

    // The actual email, the phone number, the whatever.
    @DatabaseField(columnName = Columns.CONTACT_ADDRESS_COLUMN)
    private String mContactAddress;

    // The mode of communication to be used.
    @DatabaseField(columnName = Columns.CONTACT_MODE_COLUMN, canBeNull = false)
    private ContactMode mContactMode = ContactMode.REVEAL_ONLY;

    @DatabaseField(columnName = Columns.GROUP_ID_COLUMN, foreign = true, canBeNull = false, uniqueCombo = true,
      columnDefinition = "integer references groups (_id) on delete cascade")
    private Group mGroup;

    @ForeignCollectionField(eager = false)
    private java.util.Collection<Restriction> mRestrictions;

    public String getName() { return mName; }
    public void setName(String _name) { mName = _name; }

    public String getContactAddress() { return mContactAddress; }
    public void setContactAddress(String _contactAddress) { mContactAddress = _contactAddress; }

    public ContactMode getContactMode() { return mContactMode; }
    public void setContactMode(ContactMode _contactMode) { mContactMode = _contactMode; }

    public String getLookupKey() { return mLookupKey; }
    public void setLookupKey(String _lookupKey) { mLookupKey = _lookupKey; }

    public void setGroup(Group _group) { mGroup = _group; }
    public long getGroupId() { return mGroup.getId(); }

    public long getContactId() { return mContactId; }
    public void setContactId(long contactId) { mContactId = contactId; }

    public long getRestrictionCount() { return mRestrictions == null ? 0 : mRestrictions.size(); }
    @Override
    public String toString() {
        return mName;
    }

    public String toDebugString() {
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
//
//
//    @Override
//    public int compareTo(Member _that) {
//        return String.CASE_INSENSITIVE_ORDER.compare(this.mName, _that.mName);
//    }

    public Uri getContactUri(Context _context) {
        if(mContactId == PersistableObject.UNSET_ID && mLookupKey == null)
            return null;
        Uri lookupUri = ContactsContract.Contacts.getLookupUri(mContactId, mLookupKey);
        return ContactsContract.Contacts.lookupContact(_context.getContentResolver(), lookupUri);
    }

}
