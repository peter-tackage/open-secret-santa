package com.moac.android.opensecretsanta.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.adapter.GroupListAdapter;
import com.moac.android.opensecretsanta.adapter.GroupRowDetails;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.fragment.MemberListFragment;
import com.moac.android.opensecretsanta.model.*;
import com.moac.android.opensecretsanta.util.InvalidDrawEngineException;
import com.moac.drawengine.DrawEngine;
import com.moac.drawengine.DrawFailureException;

import java.util.*;

public class NewDrawActivity extends Activity implements DrawManager {

    private static final String TAG = NewDrawActivity.class.getSimpleName();

    private static final String MEMBERS_LIST_TAG = "member_list";
    private static final String MOST_RECENT_GROUP_KEY = "most_recent_group_id";

    protected DrawerLayout mDrawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected ListView mDrawerList;
    protected DatabaseManager mDb; // shorthand.
    protected MemberListFragment mMembersListFragment;

    @Override
    public void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        mDb = OpenSecretSantaApplication.getDatabase();
        initialiseUI();
    }

    private void initialiseUI() {
        setContentView(R.layout.new_draw_activity);
        mDrawerList = (ListView) findViewById(R.id.left_drawer_list);
        mDrawerList.setAdapter(new GroupListAdapter(this));
        mDrawerList.setOnItemClickListener(new GroupListItemClickListener());

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
          this,                  /* host Activity */
          mDrawerLayout,         /* DrawerLayout object */
          R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
          R.string.drawer_open_accesshint,  /* "open drawer" description */
          R.string.drawer_close_accesshint) /* "close drawer" description */  {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(getString(R.string.app_name));
                getActionBar().setIcon(R.drawable.icon);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

             /** Called when a drawer has settled in a completely open state. */
             public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(getString(R.string.drawer_groups_title));
                getActionBar().setIcon(R.drawable.people);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                populateGroupRowDetailsList(mDrawerList);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // Add the Members List for the most recent Group
        displayInitialGroup();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if(mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // TODO Handle other action bar items...
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEditMember(long _groupId, long _memberId) {
        // TODO Launch member editor.
    }

    @Override
    public void onRestrictMember(long _groupId, long _memberId) {
        Intent intent = new Intent(this, RestrictionsActivity.class);
        intent.putExtra(Intents.GROUP_ID_INTENT_EXTRA, _groupId);
        intent.putExtra(Intents.MEMBER_ID_INTENT_EXTRA, _memberId);
        startActivity(intent);
    }

    @Override
    public void onRequestDraw(Group _group) {
        Toast.makeText(this, "Requesting Draw", Toast.LENGTH_SHORT).show();
        try {
            prepareDraw(_group);
            DrawEngine engine = OpenSecretSantaApplication.getCurrentDrawEngineInstance(getApplicationContext());
            DrawStatus status = executeDraw(engine, _group);
            Log.i(TAG, "onRequestDraw() - DrawStatus.isSuccess(): " + status.isSuccess());
            processDrawStatus(status, _group);
        } catch(InvalidDrawEngineException exp) {
            Log.e(TAG, "onRequestDraw() - Unable to load Draw Engine", exp);
            Toast.makeText(this, R.string.no_engine_error_message, Toast.LENGTH_LONG).show();
        }
    }

    private void prepareDraw(Group _group) {
        int count = mDb.deleteAllAssignmentsForGroup(_group.getId());
        Log.v(TAG, "prepareDraw() - deleted Assignment count: " + count);
        mMembersListFragment.onDrawCleared();
    }

    @Override
    public void onNotifyDraw(Group _group) {
        Toast.makeText(this, "Requesting Notify", Toast.LENGTH_SHORT).show();
    }

    private void populateGroupRowDetailsList(ListView _groupRowDetailsList) {
        // Retrieve the list of groups from database.
        List<Group> groups = OpenSecretSantaApplication.getDatabase().queryAll(Group.class);

        Log.v(TAG, "initialiseUI() - group count: " + groups.size());
        List<GroupRowDetails> groupRowDetails =  new ArrayList<GroupRowDetails>();
        for(Group g : groups) {
            List<Member> groupMembers = OpenSecretSantaApplication.getDatabase().queryAllMembersForGroup(g.getId());
            groupRowDetails.add(new GroupRowDetails(g.getId(), g.getName(), g.getCreatedAt(), groupMembers));
        }
        ((GroupListAdapter) _groupRowDetailsList.getAdapter()).update(groupRowDetails);
    }

    private void displayInitialGroup() {
        // Fetch the most recently used Group Id from preferences
        long groupId = PreferenceManager.getDefaultSharedPreferences(this).getLong(MOST_RECENT_GROUP_KEY, PersistableObject.UNSET_ID);
        if(groupId == PersistableObject.UNSET_ID)
            return;
        showGroup(groupId);
    }

    private class GroupListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> _parent, View _view, int _position, long _id) {
            showGroup(_id);
            // Highlight the selected item, update the title, and close the drawer
            mDrawerList.setItemChecked(_position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    private void showGroup(long _groupId) {
        Log.i(TAG, "showGroup() - start. groupId: " + _groupId);

        FragmentManager fragmentManager = getFragmentManager();

        // Can we find the fragment we're attempting to create?
        MemberListFragment existing = (MemberListFragment)fragmentManager.findFragmentByTag(MEMBERS_LIST_TAG);
        if (existing != null && existing.getGroupId() == _groupId)  {
            Log.i(TAG, "showGroup() - found matching required fragment");
            mMembersListFragment = existing;
            return;
        }

        // Replace existing fragment
        // Can't call replace, seems to replace ALL fragments in the layout.
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if(existing != null) {
            Log.i(TAG, "showGroup() - removing existing fragment");
            transaction.remove(mMembersListFragment);
        }

        Log.i(TAG, "showGroup() - creating new fragment");
        MemberListFragment newFragment = MemberListFragment.create(_groupId);
        transaction.add(R.id.content_frame, newFragment, MEMBERS_LIST_TAG)
          .commit();
        mMembersListFragment = newFragment;

        // Update preferences to save last viewed Group
        PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(MOST_RECENT_GROUP_KEY, _groupId).commit();
    }

    private DrawStatus executeDraw(DrawEngine _engine, Group _group) {

        DrawStatus drawStatus = new DrawStatus();

        // Build these assignments.
        Map<Long, Long> assignments;

        List<Member> members = mDb.queryAllMembersForGroup(_group.getId());
        Log.v(TAG, "executeDraw() - Group: " + _group.getId() + " has member count: " + members.size());
        Map<Long, Set<Long>> participants = new HashMap<Long, Set<Long>>();

        for(Member m : members) {
            List<Restriction> restrictions = mDb.queryAllRestrictionsForMemberId(m.getId());
            Set<Long> restrictionIds = new HashSet<Long>();
            for(Restriction r : restrictions) {
                restrictionIds.add(r.getOtherMemberId());
            }
            participants.put(m.getId(), restrictionIds);
            Log.v(TAG, "executeDraw() - " + m.getName() + "(" + m.getId() + ") has restriction count: " + restrictionIds.size());
        }

        try {
            assignments = _engine.generateDraw(participants);
            Log.v(TAG, "executeDraw() - Assignments size: " + assignments.size());
            drawStatus.setAssignments(assignments);
        } catch(DrawFailureException e) {
            // Not necessarily an error. But is draw failure regardless.
            Log.w(TAG, "executeDraw() - Couldn't produce assignments", e);
            drawStatus.setException(e);
            drawStatus.setMsg("Couldn't produce assignments");
        }

        return drawStatus;
    }

    private void processDrawStatus(DrawStatus _status, Group _group) {
        if(!_status.isSuccess()) {
            // Report failure
            Toast.makeText(this, R.string.draw_failed_message, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, R.string.draw_success_message, Toast.LENGTH_SHORT).show();

        // Flag the draw time.
        _group.setDrawDate(System.currentTimeMillis());
        mDb.update(_group);

        // Now add the corresponding draw result entries.
        for(Long m1Id : _status.getAssignments().keySet()) {

            // We are notifying m1 that they have been assigned m2.
            Member m1 = mDb.queryById(m1Id, Member.class);
            Member m2 = mDb.queryById(_status.getAssignments().get(m1Id), Member.class);

            Log.v(TAG, "saveDrawResult() - saving dre: " + m1.getName() + " - " + m2.getName() + " with: "
              + m1.getContactMode() + " " + m1.getContactAddress());

            // Create the individual Draw Result Entry
            Assignment assignment = new Assignment();
            assignment.setGiverMember(m1);
            assignment.setReceiverMember(m2);
            mDb.create(assignment);
        }
        // TODO Is this reference going to be correct on group change??
        mMembersListFragment.onDrawAvailable();
    }

    private class DrawStatus {

        private Exception mException;
        private String mMsg;
        private Map<Long, Long> mAssignments;

        private Exception getException() { return mException; }
        private void setException(Exception exception) { mException = exception; }

        private String getMsg() { return mMsg; }
        private void setMsg(String msg) { mMsg = msg; }

        private Map<Long, Long> getAssignments() { return mAssignments; }
        private void setAssignments(Map<Long, Long> assignments) {  mAssignments = assignments;}

        public boolean isSuccess() { return mAssignments != null && mException == null;}
    }
}