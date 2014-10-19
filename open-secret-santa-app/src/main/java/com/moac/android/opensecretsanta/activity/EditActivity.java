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

    protected void createEditorFragment(Bundle savedInstance) {
        if (savedInstance == null) {
            final long memberId = getIntent().getLongExtra(Intents.MEMBER_ID_INTENT_EXTRA, PersistableObject.UNSET_ID);
            MemberEditFragment fragment = MemberEditFragment.create(memberId);
            getFragmentManager().beginTransaction().add(R.id.container_content, fragment, MemberEditFragment.class.getName()).commit();
            mSaveableFragment = fragment;
        } else {
            mSaveableFragment = (Saveable) getFragmentManager().findFragmentByTag(MemberEditFragment.class.getName());
        }
    }

}
