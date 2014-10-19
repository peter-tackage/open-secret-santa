package com.moac.android.opensecretsanta.util.validator;

import android.text.TextUtils;

import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Group;

public class GroupNameValidator implements Validator {
    private final DatabaseManager mDb;
    private final String mGroupName;
    private final long mGroupId;
    private String mMsg;

    public GroupNameValidator(DatabaseManager db, long groupId, String groupName) {
        mDb = db;
        mGroupId = groupId;
        mGroupName = groupName;
    }

    @Override
    public boolean isValid() {
        // Avoid hitting the database if possible.
        if (TextUtils.isEmpty(mGroupName)) {
            mMsg = "Name can't be empty";
            return false;
        }
        // Check that group there is no existing group with that name (or it it's the same group)
        // This allows for case change for the same group
        Group existing = mDb.queryForGroupWithName(mGroupName);
        boolean isValid = existing == null || existing.getId() == mGroupId;
        if (!isValid) {
            mMsg = "This name already exists";
        }
        return isValid;
    }

    @Override
    public String getMsg() {
        return mMsg;
    }
}
