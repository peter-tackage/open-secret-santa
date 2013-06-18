package com.moac.android.opensecretsanta.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.adapter.GroupListAdapter;
import com.moac.android.opensecretsanta.adapter.MemberRowDetails;
import com.moac.android.opensecretsanta.fragment.MemberListFragment;
import com.moac.android.opensecretsanta.model.Group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewDrawActivity extends Activity {

    private static final String TAG = NewDrawActivity.class.getSimpleName();

    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    ListView mDrawerList;

    public void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        initialiseUI();
    }

    private void initialiseUI() {
        setContentView(R.layout.new_draw_activity);

        List<Group> groups = OpenSecretSantaApplication.getDatabase().queryAll(Group.class);
        Log.v(TAG, "initialiseUI() - group count: " + groups.size());
        mDrawerList = (ListView) findViewById(R.id.left_drawer_list);
        mDrawerList.setAdapter(new GroupListAdapter(this, groups));
        mDrawerList.setOnItemClickListener(new GroupListItemClickListener());

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
          this,                  /* host Activity */
          mDrawerLayout,         /* DrawerLayout object */
          R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
          R.string.drawer_open,  /* "open drawer" description */
          R.string.drawer_close  /* "close drawer" description */
        ) {

        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
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
        // TODO Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(TAG, "onStart() - start");
        // We no longer expose the concept of the Group to the user.
        Log.v(TAG, "onStart() - end");
    }

    private class GroupListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> _parent, View _view, int _position, long _id) {
            showGroup(_position, _id);
        }
    }

    private void showGroup(int _position, long _groupId) {
        Fragment fragment = MemberListFragment.create(_groupId);
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
          .replace(R.id.content_frame, fragment)
          .commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(_position, true);
        mDrawerLayout.closeDrawer(mDrawerList);

    }


}