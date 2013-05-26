package com.moac.android.opensecretsanta.types;

import android.provider.BaseColumns;

public class Group extends PersistentModel {

    // GROUP TABLE COLUMNS
    public static interface GroupColumns extends BaseColumns {

        public static final String NAME_COLUMN = "NAME";
        public static final String IS_READY = "IS_READY";

        public static final String DEFAULT_SORT_ORDER = NAME_COLUMN + " ASC";

        public static final String[] ALL = {
          _ID,
          NAME_COLUMN,
          IS_READY
        };
    }

    private String mName;
    private boolean mReady = false; // is the group ready to be notified.

    public String getName() { return mName; }
    public void setName(String name) { mName = name; }
    public void setReady(boolean ready) { mReady = ready; }
    public boolean isReady() { return mReady; }

}
