package com.moac.android.opensecretsanta.activity;

import android.app.*;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.adapter.GroupListAdapter;
import com.moac.android.opensecretsanta.adapter.GroupRowDetails;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.draw.DrawExecutor;
import com.moac.android.opensecretsanta.draw.MemberEditor;
import com.moac.android.opensecretsanta.fragment.DrawExecutorFragment;
import com.moac.android.opensecretsanta.fragment.MemberListFragment;
import com.moac.android.opensecretsanta.fragment.NotifyFragment;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.PersistableObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements MemberListFragment.FragmentContainer, MemberEditor {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String MEMBERS_LIST_FRAGMENT_TAG = "MemberListFragment";
    private static final String DRAW_EXECUTOR_FRAGMENT_TAG = "DrawExecutorFragment";
    private static final String NOTIFY_FRAGMENT_TAG = "NotifyFragment";
    private static final String MOST_RECENT_GROUP_KEY = "most_recent_group_id";

    protected DrawerLayout mDrawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected ListView mDrawerList;
    protected DatabaseManager mDb; // shorthand.
    protected MemberListFragment mMembersListFragment;
    protected DrawExecutorFragment mDrawExecutorFragment;

    @Override
    public void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        mDb = OpenSecretSantaApplication.getDatabase();

        // Find existing worker fragment
        FragmentManager fm = getFragmentManager();
        mDrawExecutorFragment = (DrawExecutorFragment) fm.findFragmentByTag(DRAW_EXECUTOR_FRAGMENT_TAG);

        if (mDrawExecutorFragment == null) {
            mDrawExecutorFragment = DrawExecutorFragment.create();
            fm.beginTransaction().add(mDrawExecutorFragment, DRAW_EXECUTOR_FRAGMENT_TAG).commit();
        }
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
                R.string.drawer_close_accesshint) /* "close drawer" description */ {

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
        if (mDrawerToggle.onOptionsItemSelected(item)) {
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
        Intent intent = new Intent(MainActivity.this, RestrictionsActivity.class);
        intent.putExtra(Intents.GROUP_ID_INTENT_EXTRA, _groupId);
        intent.putExtra(Intents.MEMBER_ID_INTENT_EXTRA, _memberId);
        // Activity options is since API 16.
        // Got this idea from Android Dev Bytes video - https://www.youtube.com/watch?v=Ho8vk61lVIU
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        } else {
            Bundle translateBundle = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
            startActivity(intent, translateBundle);
        }
    }

    @Override
    public void onNotifyDraw(Group _group, long[] _memberIds) {
        Log.i(TAG, "onNotifyDraw() - Requesting Notify member set");
        DialogFragment dialog = NotifyFragment.create(_group.getId(), _memberIds);
        dialog.show(getFragmentManager(), NOTIFY_FRAGMENT_TAG);
    }

    @Override
    public void onNotifyDraw(Group _group) {
        Log.i(TAG, "onNotifyDraw() - Requesting Notify entire Group");
        List<Member> members = mDb.queryAllMembersForGroup(_group.getId());
        // TODO Handle send all.
    }

    private void populateGroupRowDetailsList(ListView _groupRowDetailsList) {
        // Retrieve the list of groups from database.
        List<Group> groups = OpenSecretSantaApplication.getDatabase().queryAll(Group.class);

        Log.v(TAG, "initialiseUI() - group count: " + groups.size());
        List<GroupRowDetails> groupRowDetails = new ArrayList<GroupRowDetails>();
        for (Group g : groups) {
            List<Member> groupMembers = OpenSecretSantaApplication.getDatabase().queryAllMembersForGroup(g.getId());
            groupRowDetails.add(new GroupRowDetails(g.getId(), g.getName(), g.getCreatedAt(), groupMembers));
        }
        ((GroupListAdapter) _groupRowDetailsList.getAdapter()).update(groupRowDetails);
    }

    private void displayInitialGroup() {
        // Fetch the most recently used Group Id from preferences
        long groupId = PreferenceManager.getDefaultSharedPreferences(this).getLong(MOST_RECENT_GROUP_KEY, PersistableObject.UNSET_ID);
        if (groupId == PersistableObject.UNSET_ID)
            return;
        showGroup(groupId);
    }

    @Override
    public DrawExecutor getDrawExecutor() {
        return mDrawExecutorFragment;
    }

    @Override
    public MemberEditor getMemberEditor() {
        // FIXME for now.
        return this;
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
        MemberListFragment existing = (MemberListFragment) fragmentManager.findFragmentByTag(MEMBERS_LIST_FRAGMENT_TAG);
        if (existing != null && existing.getGroupId() == _groupId) {
            Log.i(TAG, "showGroup() - found matching required fragment");
            mMembersListFragment = existing;
            return;
        }

        // Replace existing fragment
        // Can't call replace, seems to replace ALL fragments in the layout.
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (existing != null) {
            Log.i(TAG, "showGroup() - removing existing fragment");
            transaction.remove(mMembersListFragment);
        }

        Log.i(TAG, "showGroup() - creating new fragment");
        MemberListFragment newFragment = MemberListFragment.create(_groupId);
        transaction.add(R.id.content_frame, newFragment, MEMBERS_LIST_FRAGMENT_TAG)
                .commit();
        mMembersListFragment = newFragment;

        // Update preferences to save last viewed Group
        PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(MOST_RECENT_GROUP_KEY, _groupId).commit();
    }

}