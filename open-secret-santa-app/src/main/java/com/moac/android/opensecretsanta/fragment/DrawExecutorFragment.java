package com.moac.android.opensecretsanta.fragment;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.content.BusProvider;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.draw.AssignmentsEvent;
import com.moac.android.opensecretsanta.draw.DrawExecutor;
import com.moac.android.opensecretsanta.draw.DrawResultEvent;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.Restriction;
import com.moac.android.opensecretsanta.draw.DrawEngineFactory;
import com.moac.android.opensecretsanta.draw.InvalidDrawEngineException;
import com.moac.drawengine.DrawEngine;
import com.moac.drawengine.DrawFailureException;
import com.squareup.otto.Bus;

import java.util.*;

public class DrawExecutorFragment extends Fragment implements DrawExecutor {

    // TODO Inject
    DatabaseManager mDb;
    Bus mBus;

    private static final String TAG = DrawExecutorFragment.class.getSimpleName();

    public static DrawExecutorFragment create() {
        DrawExecutorFragment fragment = new DrawExecutorFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDb = OpenSecretSantaApplication.getDatabase();
        mBus = BusProvider.getInstance();
    }

    @Override
    public void requestDraw(Group _group) {
        Log.i(TAG, "requestDraw() - Requesting Draw");

        // Find the current DrawEngine to use
        DrawEngine engine;
        try {
            engine = getCurrentDrawEngine();
        } catch (InvalidDrawEngineException exp) {
            Log.e(TAG, "requestDraw() - Unable to load Draw Engine", exp);
            mBus.post(DrawResultEvent.failure(_group.getId(), exp));
            return;
        }

        // We have a DrawEngine, prepare to draw.
        // TODO Uncertain whether we should actually do this at all.
        invalidateAssignments(_group);

        // Execute the draw
        DrawResultEvent result = executeDraw(engine, _group);

        // TODO Should we post separate events for pass or fail - we already know.
        // Persist the generated assignments
        Log.i(TAG, "requestDraw() - success: " + result.isSuccess());
        if (result.isSuccess()) {
            setAssignments(_group, result.getAssignments());
        }

        // Notify of draw result.
        mBus.post(result);
    }

    // Returns an instance of the currently preferred DrawEngine
    // TODO This should be somewhere - try to keep this lean.
    private DrawEngine getCurrentDrawEngine() throws InvalidDrawEngineException {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defaultName = getActivity().getString(R.string.defaultDrawEngine);
        String classname = prefs.getString("engine_preference",
                defaultName);

        Log.i(TAG, "getCurrentDrawEngine() - setting draw engine to: " + classname);

        try {
            return DrawEngineFactory.createDrawEngine(classname);
        } catch (InvalidDrawEngineException e) {
            // Error: If we weren't attempting to load the default name, then try that instead
            if (!classname.equals(defaultName)) {
                Log.w(TAG, "Failed to initialise draw engine class: " + classname);
                try {
                    // Try to set the default then.
                    DrawEngine engine = DrawEngineFactory.createDrawEngine(defaultName);
                    // Success - update preference to use the default.
                    prefs.edit().putString("engine_preference", defaultName).commit();
                    return engine;
                } catch (InvalidDrawEngineException ideexp2) {
                    Log.e(TAG, "Unable to initialise default draw engine class: " + classname, ideexp2);
                    throw ideexp2;
                }
            }
            throw e;
        }
    }

    private void invalidateAssignments(Group _group) {
        long count = mDb.deleteAllAssignmentsForGroup(_group.getId());
        _group.setDrawDate(Group.UNSET_DATE);
        mDb.update(_group);
        Log.v(TAG, "invalidateAssignments() - deleted Assignment count: " + count);
        mBus.post(new AssignmentsEvent(_group.getId()));
    }

    private DrawResultEvent executeDraw(DrawEngine _engine, Group _group) {
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

        try {
            assignments = _engine.generateDraw(participants);
            Log.v(TAG, "executeDraw() - Assignments size: " + assignments.size());
            return DrawResultEvent.success(_group.getId(), assignments);
        } catch (DrawFailureException e) {
            // Not necessarily an error. But is draw failure regardless.
            Log.w(TAG, "executeDraw() - Couldn't produce assignments", e);
            return DrawResultEvent.failure(_group.getId(), e, "Couldn't produce assignments");
        }
    }

    private void setAssignments(Group _group, Map<Long, Long> _assignments) {
        // Flag the draw time.
        _group.setDrawDate(System.currentTimeMillis());
        mDb.update(_group);

        // Now add the corresponding draw result entries.
        for (Long m1Id : _assignments.keySet()) {

            // We are notifying m1 that they have been assigned m2.
            Member m1 = mDb.queryById(m1Id, Member.class);
            Member m2 = mDb.queryById(_assignments.get(m1Id), Member.class);

            Log.v(TAG, "saveDrawResult() - saving Assignment: " + m1.getName() + " - " + m2.getName() + " with: "
                    + m1.getContactMode() + " " + m1.getContactAddress());

            // Create the individual Draw Result Entry
            Assignment assignment = new Assignment();
            assignment.setGiverMember(m1);
            assignment.setReceiverMember(m2);
            mDb.create(assignment);
        }
    }

}

