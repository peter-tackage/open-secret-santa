package com.moac.android.opensecretsanta.model;

import com.j256.ormlite.field.DatabaseField;

public abstract class PersistableObject {

    public static final long UNSET_ID = -1;
    public static final long UNSET_DATE = -1;

    @DatabaseField(columnName = Columns._ID, generatedId = true, unique = true, canBeNull = false)
    private long mId = UNSET_ID;

    public interface Columns {
        public static final String _ID = "_id";
    }

    public long getId() { return mId; }

}
