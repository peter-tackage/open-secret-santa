package com.moac.android.opensecretsanta.content;

import com.moac.android.opensecretsanta.model.Assignment;

public class AssignmentStatusEvent {

    private final Assignment mAssignment;

    public AssignmentStatusEvent(Assignment _assignment) {
        mAssignment = _assignment;
    }

    public Assignment getAssignment() { return mAssignment; }
}
