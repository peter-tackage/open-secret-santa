package com.moac.android.opensecretsanta.draw;

import android.util.Log;

import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.Restriction;
import com.moac.drawengine.DrawEngine;
import com.moac.drawengine.DrawFailureException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;

public class DefaultDrawExecutor implements DrawExecutor {

    private static final String TAG = DefaultDrawExecutor.class.getSimpleName();

    private final DatabaseManager mDb;

    public DefaultDrawExecutor(DatabaseManager dm) {
        mDb = dm;
    }

    @Override
    public Observable<DrawResultEvent> requestDraw(final DrawEngine _engine, final Group _group) {

        return Observable.create(new Observable.OnSubscribe<DrawResultEvent>() {

            @Override
            public void call(Subscriber<? super DrawResultEvent> subscriber) {
                Log.i(TAG, "requestDraw() - Requesting Draw");

                // Clear in case something failed uncleanly
                invalidateAssignments(_group);

                try {
                    // Execute the draw
                    DrawResultEvent result = executeDraw(_engine, _group);
                    saveAssignments(_group, result.getAssignments());
                    subscriber.onNext(result);
                    subscriber.onCompleted();
                } catch (DrawFailureException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private void invalidateAssignments(Group _group) {
        long count = mDb.deleteAllAssignmentsForGroup(_group.getId());
        _group.setDrawDate(Group.UNSET_DATE);
        mDb.update(_group);
        Log.v(TAG, "invalidateAssignments() - deleted Assignment count: " + count);
    }

    private DrawResultEvent executeDraw(DrawEngine _engine, Group _group) throws DrawFailureException {
        Map<Long, Long> assignments;

        List<Member> members = mDb.queryAllMembersForGroup(_group.getId());
        Log.v(TAG, "executeDraw() - Group: " + _group.getId() + " has member count: " + members.size());
        Map<Long, Set<Long>> participants = new HashMap<Long, Set<Long>>();

        for (Member m : members) {
            List<Restriction> restrictions = mDb.queryAllRestrictionsForMemberId(m.getId());
            Set<Long> restrictionIds = new HashSet<Long>();
            for (Restriction r : restrictions) {
                restrictionIds.add(r.getOtherMemberId());
            }
            participants.put(m.getId(), restrictionIds);
            Log.v(TAG, "executeDraw() - " + m.getName() + "(" + m.getId() + ") has restriction count: " + restrictionIds.size());
        }

        assignments = _engine.generateDraw(participants);
        Log.v(TAG, "executeDraw() - Assignments size: " + assignments.size());
        return DrawResultEvent.success(_group.getId(), assignments);
    }

    private void saveAssignments(Group _group, Map<Long, Long> _assignments) {
        // Flag the draw time.
        _group.setDrawDate(System.currentTimeMillis());
        mDb.update(_group);

        // Now add the corresponding draw result entries.
        for (Long m1Id : _assignments.keySet()) {

            // We are notifying m1 that they have been assigned m2.
            Member m1 = mDb.queryById(m1Id, Member.class);
            Member m2 = mDb.queryById(_assignments.get(m1Id), Member.class);

            Log.v(TAG, "saveDrawResult() - saving Assignment: " + m1.getName() + " - " + m2.getName() + " with: "
                    + m1.getContactMethod() + " " + m1.getContactDetails());

            // Create the individual Draw Result Entry
            Assignment assignment = new Assignment();
            assignment.setGiverMember(m1);
            assignment.setReceiverMember(m2);
            mDb.create(assignment);
        }
    }
}
