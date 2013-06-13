package com.moac.android.opensecretsanta.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.fragment.MemberListFragment;
import com.moac.android.opensecretsanta.types.Group;

import java.util.List;

public class NewDrawActivity extends Activity {

    private static final String TAG = NewDrawActivity.class.getSimpleName();

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialiseUI();
    }

    private void initialiseUI() {
        setContentView(R.layout.new_draw_activity);
        mPager = (ViewPager)findViewById(R.id.draws_pager);
        List<Group> groups = OpenSecretSantaApplication.getDatabase().queryAll(Group.class);
        mPagerAdapter = new DrawPagerAdapter(getFragmentManager(), groups);
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(TAG, "onStart() - start");

        // We no longer expose the concept of the Group to the user.

        Log.v(TAG, "onStart() - end");
    }

    private static class DrawPagerAdapter extends FragmentStatePagerAdapter {

        private List<Group> mGroups;

        public DrawPagerAdapter(FragmentManager _fm, List<Group> _groups) {
            super(_fm);
            mGroups = _groups;
        }

        @Override
        public Fragment getItem(int position) {
            return MemberListFragment.create(mGroups.get(position).getId());
        }

        @Override
        public int getCount() {
            return mGroups.size();
        }

    }


}