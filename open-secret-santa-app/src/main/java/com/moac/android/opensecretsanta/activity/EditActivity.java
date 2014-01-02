package com.moac.android.opensecretsanta.activity;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.moac.android.inject.dagger.InjectingActivity;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.fragment.MemberEditFragment;
import com.moac.android.opensecretsanta.model.PersistableObject;

/**
 * This activity doesn't do much but supply a custom action bar and host
 * the MemberEditFragment.
 */
public class EditActivity extends InjectingActivity {

    protected MemberEditFragment mMemberEditFragment;

    @Override
    public void onCreate(Bundle _savedInstance) {
        super.onCreate(_savedInstance);
        setContentView(R.layout.activity_generic_editor);

        // Add the editor fragment
        final long memberId = getIntent().getLongExtra(Intents.MEMBER_ID_INTENT_EXTRA, PersistableObject.UNSET_ID);

        mMemberEditFragment = MemberEditFragment.create(memberId);
        getFragmentManager().beginTransaction().add(R.id.content_frame, mMemberEditFragment).commit();

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
                    if(mMemberEditFragment.doSaveAction())
                        finish();
                }
            });
            // Show the custom action bar but hide the home icon and title
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
              ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setCustomView(customActionBarView);
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        if(mMemberEditFragment.doSaveAction())
            super.onBackPressed();
    }
}
