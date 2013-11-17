package com.moac.android.opensecretsanta.model.version2;

/**
 * Created with IntelliJ IDEA.
 * User: amelysh
 * Date: 13.11.13
 * Time: 23:18
 * To change this template use File | Settings | File Templates.
 */

import android.provider.BaseColumns;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.moac.android.opensecretsanta.model.PersistableObject;

@DatabaseTable(tableName = MemberVersion2.TABLE_NAME)
public final class MemberVersion2 extends PersistableObject {

    public static final String TAG = "Member";
    public static final String TABLE_NAME =  "members";

    public static interface Columns extends BaseColumns {

        public static final String LOOKUP_KEY = "LOOKUP_KEY";
        public static final String NAME_COLUMN = "NAME";
        public static final String CONTACT_MODE_COLUMN = "CONTACT_MODE";
        public static final String CONTACT_DETAIL_COLUMN = "CONTACT_DETAIL";
        public static final String GROUP_ID_COLUMN = "GROUP_ID";

        public static final String DEFAULT_SORT_ORDER = NAME_COLUMN + " ASC";

        public static final String[] ALL = {
                _ID,
                LOOKUP_KEY,
                NAME_COLUMN,
                CONTACT_MODE_COLUMN,
                CONTACT_DETAIL_COLUMN,
                GROUP_ID_COLUMN
        };
    }

    @DatabaseField(columnName = Columns.LOOKUP_KEY)
    private String mLookupKey;

    // The participant name
    @DatabaseField(columnName = Columns.NAME_COLUMN, canBeNull = false)
    private String mName;

    // the email, the  phone number, the whatever.
    @DatabaseField(columnName = Columns.CONTACT_DETAIL_COLUMN)
    private String mContactDetail;

    // The types of communication to be used.
    @DatabaseField(columnName = Columns.CONTACT_MODE_COLUMN, canBeNull = false)
    private int mContactMode = ConstantsVersion2.NAME_ONLY_CONTACT_MODE;

    @DatabaseField(columnName = Columns.GROUP_ID_COLUMN, foreign = true, canBeNull = false,
            columnDefinition = "integer references groups (_id) on delete cascade")
    private GroupVersion2 mGroup;


    public String getName() { return mName; }

    public void setName(String _name) { mName = _name; }

    public String getContactDetail() { return mContactDetail; }

    public void setContactDetail(String _contactDetail) { mContactDetail = _contactDetail; }

    public int getContactMode() { return mContactMode; }

    public void setContactMode(int _contactMode) { mContactMode = _contactMode; }

    public String getLookupKey() { return mLookupKey; }

    public void setLookupKey(String _lookupKey) { mLookupKey = _lookupKey; }

    public void setGroup(GroupVersion2 group) { mGroup = group; }

    public long getGroupId() { return mGroup.getId(); }

}