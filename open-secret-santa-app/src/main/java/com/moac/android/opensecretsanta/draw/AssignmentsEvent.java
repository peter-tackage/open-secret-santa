package com.moac.android.opensecretsanta.draw;

public class AssignmentsEvent {
    private final long mGroupId;

    public AssignmentsEvent(long groupId) {
        mGroupId = groupId;
    }

    public long getGroupId() {
        return mGroupId;
    }
}
