package com.moac.android.opensecretsanta.util;

import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Member;

public class MemberNameValidator implements Validator {
    private final String mName;
    private final DatabaseManager mDb;
    private final long mGroupId;
    private final long mMemberId;

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
            return false;
        }
        Member existing = mDb.queryMemberWithNameForGroup(mGroupId, mName);
        return existing == null || existing.getId() == mMemberId;
    }
}
