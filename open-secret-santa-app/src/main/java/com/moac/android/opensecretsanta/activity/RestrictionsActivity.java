package com.moac.android.opensecretsanta.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.fragment.RestrictionsListFragment;
import com.moac.android.opensecretsanta.model.PersistableObject;

/**
 * This activity doesn't do much but supply a custom action bar and host
 * the RestrictionsListFragment.
 */
public class RestrictionsActivity extends Activity {

    protected RestrictionsListFragment mRestrictionsListFragment;

    @Override
    public void onCreate(Bundle _savedInstance) {
        super.onCreate(_savedInstance);
        setContentView(R.layout.activity_generic_editor);

        // Add the restrictions list fragment
        final long groupId = getIntent().getLongExtra(Intents.GROUP_ID_INTENT_EXTRA, PersistableObject.UNSET_ID);
        final long memberId = getIntent().getLongExtra(Intents.MEMBER_ID_INTENT_EXTRA, PersistableObject.UNSET_ID);

        mRestrictionsListFragment = RestrictionsListFragment.create(groupId, memberId);
        getFragmentManager().beginTransaction().add(R.id.content_frame, mRestrictionsListFragment).commit();

        // Action bar should always exist for our API levels.
        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            // Inflate a custom action bar that contains the "Done" button for saving changes
            LayoutInflater inflater = (LayoutInflater) getSystemService
              (Context.LAYOUT_INFLATER_SERVICE);
            View customActionBarView = inflater.inflate(R.layout.generic_editor_custom_action_bar, null);
            View saveMenuItem = customActionBarView.findViewById(R.id.save_menu_item);
            saveMenuItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRestrictionsListFragment.doSaveAction();
                    finish();
                }
            });
            // Show the custom action bar but hide the home icon and title
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
              ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setCustomView(customActionBarView);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        mRestrictionsListFragment.doSaveAction();
        super.onBackPressed();
    }
}
