package com.moac.android.opensecretsanta.activity;

import android.os.Bundle;

import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.fragment.MemberEditFragment;
import com.moac.android.opensecretsanta.fragment.Saveable;
import com.moac.android.opensecretsanta.model.PersistableObject;

/**
 * Host the MemberEditFragment
 */
public class EditActivity extends BaseEditorActivity {

    private static final String FRAGMENT_TAG = MemberEditFragment.class.getName();

    protected void createEditorFragment(Bundle savedInstance) {
        if (savedInstance == null) {
            final long memberId = getIntent().getLongExtra(Intents.MEMBER_ID_INTENT_EXTRA, PersistableObject.UNSET_ID);
            MemberEditFragment fragment = MemberEditFragment.create(memberId);
            getFragmentManager().beginTransaction().add(R.id.container_fragment_content, fragment, FRAGMENT_TAG).commit();
            mSaveableFragment = fragment;
        } else {
            mSaveableFragment = (Saveable) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        }
    }

}
