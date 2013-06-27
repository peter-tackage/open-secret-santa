package com.moac.android.opensecretsanta.activity;

import android.app.Activity;
import android.app.Fragment;
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
import android.widget.*;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.adapter.GroupListAdapter;
import com.moac.android.opensecretsanta.fragment.MemberListFragment;
import com.moac.android.opensecretsanta.model.DrawResult;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.PersistableObject;

import java.util.List;

public class NewDrawActivity extends Activity implements DrawManager {

    private static final String TAG = NewDrawActivity.class.getSimpleName();

    private static final String MEMBERS_LIST_TAG = "member_list";
    private static final String MOST_RECENT_GROUP_KEY = "most_recent_group_id";

    protected DrawerLayout mDrawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected ListView mDrawerList;

    @Override
    public void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
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
          R.string.drawer_close_accesshint  /* "close drawer" description */
        );

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        populateGroupsList(mDrawerList);

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
    }

    @Override
    public void onNotifyDraw(DrawResult _result) {
        Toast.makeText(this, "Requesting Notify", Toast.LENGTH_SHORT).show();
    }

    private void populateGroupsList(ListView _groupsList) {
        // Retrieve the list of groups from database.
        List<Group> groups = OpenSecretSantaApplication.getDatabase().queryAll(Group.class);
        Log.v(TAG, "initialiseUI() - group count: " + groups.size());
        ((GroupListAdapter) _groupsList.getAdapter()).update(groups);
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
        Log.i(TAG, "showGroup() - start");

        Fragment fragment = MemberListFragment.create(_groupId);
        // Replace existing fragment
        // Can't call replace, seems to replace ALL fragments in the layout.
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment existing = fragmentManager.findFragmentByTag(MEMBERS_LIST_TAG);
        if(existing != null)
            transaction.remove(existing);
        transaction.add(R.id.content_frame, fragment, MEMBERS_LIST_TAG)
          .commit();

        // Update preferences to save last viewed Group
        PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(MOST_RECENT_GROUP_KEY, _groupId).commit();
    }

}