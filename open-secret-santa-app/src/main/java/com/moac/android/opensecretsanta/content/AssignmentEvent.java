package com.moac.android.opensecretsanta.content;

import com.moac.android.opensecretsanta.model.Assignment;

/**
 * Created with IntelliJ IDEA.
 * User: peter
 * Date: 7/2/13
 * Time: 12:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class AssignmentEvent {

    private final Assignment mAssignment;

    public AssignmentEvent(Assignment _assignment) {
        mAssignment = _assignment;
    }

    public Assignment getAssignment() { return mAssignment; }
}
