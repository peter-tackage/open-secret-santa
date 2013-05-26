package com.moac.android.opensecretsanta.types;

import android.provider.BaseColumns;

public final class Group {

    // GROUP TABLE COLUMNS
    public static final class GroupColumns implements BaseColumns {

        // This class cannot be instantiated
        private GroupColumns() {
        }

        public static final String NAME_COLUMN = "NAME";
        public static final String IS_READY = "IS_READY";

        public static final String DEFAULT_SORT_ORDER = NAME_COLUMN + " ASC";

        public static final String[] ALL = {
          _ID,
          NAME_COLUMN,
          IS_READY
        };
    }

    private long mId = -1;
    private String mName;
    private boolean mReady = false; // is the group ready to be notified.

    public Group(String name) {
        mName = name;
    }

    public Group(long id, String name, boolean ready) {
        mId = id;
        mName = name;
        mReady = ready;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setReady(boolean ready) {
        this.mReady = ready;
    }

    public boolean isReady() {
        return this.mReady;
    }

    public long getId() {
        return this.mId;
    }
}
