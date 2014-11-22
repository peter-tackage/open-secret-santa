package com.moac.android.opensecretsanta.activity;

import android.os.Bundle;

import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.fragment.RestrictionsListFragment;
import com.moac.android.opensecretsanta.fragment.Saveable;
import com.moac.android.opensecretsanta.model.PersistableObject;

/**
 * Host the RestrictionsListFragment
 */
public class RestrictionsActivity extends BaseEditorActivity {

    private static final String FRAGMENT_TAG = RestrictionsListFragment.class.getName();

    @Override
    protected void createEditorFragment(Bundle savedInstance) {
        if (savedInstance == null) {
            // Add the restrictions list fragment
            final long groupId = getIntent().getLongExtra(Intents.GROUP_ID_INTENT_EXTRA, PersistableObject.UNSET_ID);
            final long memberId = getIntent().getLongExtra(Intents.MEMBER_ID_INTENT_EXTRA, PersistableObject.UNSET_ID);
            RestrictionsListFragment fragment = RestrictionsListFragment.create(groupId, memberId);
            getFragmentManager().beginTransaction().add(R.id.container_fragment_content, fragment, FRAGMENT_TAG).commit();
            mSaveableFragment = fragment;
        } else {
            mSaveableFragment = (Saveable) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        }
    }
}
