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
        // Note that CONTACT_MODE is deprecated since v3
        public static final String CONTACT_METHOD_COLUMN = "CONTACT_METHOD";
        public static final String CONTACT_DETAIL_COLUMN = "CONTACT_DETAIL";
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
    @DatabaseField(columnName = Columns.CONTACT_DETAIL_COLUMN)
    private String mContactDetails;

    // The mode of communication to be used.
    @DatabaseField(columnName = Columns.CONTACT_METHOD_COLUMN, canBeNull = false)
    private ContactMethod mContactMethod = ContactMethod.REVEAL_ONLY;

    @DatabaseField(columnName = Columns.GROUP_ID_COLUMN, foreign = true, canBeNull = false, uniqueCombo = true,
      columnDefinition = "integer references groups (_id) on delete cascade")
    private Group mGroup;

    @ForeignCollectionField(eager = false)
    private java.util.Collection<Restriction> mRestrictions;

    public String getName() { return mName; }
    public void setName(String _name) { mName = _name; }

    public String getContactDetails() { return mContactDetails; }
    public void setContactDetails(String _contactDetails) { mContactDetails = _contactDetails; }

    public ContactMethod getContactMethod() { return mContactMethod; }
    public void setContactMethod(ContactMethod _contactMethod) { mContactMethod = _contactMethod; }

    public String getLookupKey() { return mLookupKey; }
    public void setLookupKey(String _lookupKey) { mLookupKey = _lookupKey; }

    public void setGroup(Group _group) { mGroup = _group; }
    public long getGroupId() { return mGroup.getId(); }

    public long getContactId() { return mContactId; }
    public void setContactId(long contactId) { mContactId = contactId; }

    public long getRestrictionCount() { return mRestrictions == null ? 0 : mRestrictions.size(); }

    public String toDebugString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Id: ");
        sb.append(getId());
        sb.append(", Name: ");
        sb.append(getName());
        sb.append(", Contact Details: ");
        sb.append(getContactDetails());
        sb.append(", Contact Method: ");
        sb.append(getContactMethod());
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
//          (null == this.mContactDetails ? (this.mContactDetails == that.mContactDetails) : this.mContactDetails.equals(that.mContactDetails))
//            &&
//            (null == this.mLookupKey ? (this.mLookupKey == that.mLookupKey) : this.mLookupKey.equals(that.mLookupKey))
//            &&
//            (null == this.mName ? (this.mName == that.mName) : this.mName.equals(that.mName))
//            &&
//            this.mContactMethod == that.mContactMethod;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Member member = (Member) obj;
        return (mContactId == member.getContactId() &&
                (mLookupKey == null ? member.getLookupKey() == null : mLookupKey.equals(member.getLookupKey())) &&
                (mContactDetails == null ?  member.getContactDetails() == null : mContactDetails.equals(member.getContactDetails()) &&
                mGroup.getId() == member.getGroupId() &&
                mContactMethod == member.getContactMethod() &&
                mName.equals(member.getName())));
    }

    public static class MemberBuilder {
        Member mMember;

        public MemberBuilder() {
            mMember = new Member();
        }

        public MemberBuilder withMemberId(long _id) {
            mMember.setId(_id);
            return this;
        }

        public MemberBuilder withContactId(long contactId) {
            mMember.setContactId(contactId);
            return this;
        }

        public MemberBuilder withLookupKey(String key) {
            mMember.setLookupKey(key);
            return this;
        }

        public MemberBuilder withName(String name) {
            mMember.setName(name);
            return this;
        }

        public MemberBuilder withContactDetails(String details) {
            mMember.setContactDetails(details);
            return this;
        }

        public MemberBuilder withContactMethod(ContactMethod method) {
            mMember.setContactMethod(method);
            return this;
        }

        public MemberBuilder withGroup(Group group) {
            mMember.setGroup(group);
            return this;
        }

        public Member build() { return mMember; }
    }


}
