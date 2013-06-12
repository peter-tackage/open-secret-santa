package com.moac.android.opensecretsanta.activity;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import com.moac.android.opensecretsanta.R;

public class NewDrawActivity extends Activity {

    private static final String TAG = NewDrawActivity.class.getSimpleName();

    private ListFragment mMemberListFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialiseUI();
    }

    private void initialiseUI() {
        setContentView(R.layout.new_draw_activity);
        mMemberListFragment = (ListFragment)getFragmentManager().findFragmentById(R.id.memberListFragment);
        mMemberListFragment.setEmptyText(getString(R.string.empty_member_list_label));
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(TAG, "onStart() - start");

        // We no longer expose the concept of the Group to the user.

        Log.v(TAG, "onStart() - end");
    }

}