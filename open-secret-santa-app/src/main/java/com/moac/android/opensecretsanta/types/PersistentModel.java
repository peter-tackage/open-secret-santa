package com.moac.android.opensecretsanta.types;

public abstract class PersistentModel {

    public static final long UNSET_ID = -1;

    protected long mId = UNSET_ID;

    public long getId() { return mId; }
    public void setId(long _id) { mId = _id; }
}
