package com.moac.android.opensecretsanta.content;

import com.moac.android.opensecretsanta.model.Assignment;

public class NotifyStatusEvent {

    private final Assignment mAssignment;

    public NotifyStatusEvent(Assignment _assignment) {
        mAssignment = _assignment;
    }

    public Assignment getAssignment() { return mAssignment; }
}
