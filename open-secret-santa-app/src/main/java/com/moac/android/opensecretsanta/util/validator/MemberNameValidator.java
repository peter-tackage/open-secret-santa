package com.moac.android.opensecretsanta.util.validator;

import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Member;

public class MemberNameValidator implements Validator {
    private final String mName;
    private final DatabaseManager mDb;
    private final long mGroupId;
    private final long mMemberId;
    private String mMsg;

    public MemberNameValidator(DatabaseManager db, long groupId, long memberId, String name) {
        mDb = db;
        mGroupId = groupId;
        mMemberId = memberId;
        mName = name;
    }

    @Override
    public boolean isValid() {
        // Avoid hitting the database if possible.
        if(mName == null || mName.isEmpty()) {
            mMsg = "Something doesn't seem right!";
            return false;
        }
        Member existing = mDb.queryMemberWithNameForGroup(mGroupId, mName);
        boolean isSaveable = existing == null || existing.getId() == mMemberId;
        if(!isSaveable) {
            mMsg = "This name already exists";
        }
        return isSaveable;
    }

    @Override
    public String getMsg() {
        return mMsg;
    }
}
