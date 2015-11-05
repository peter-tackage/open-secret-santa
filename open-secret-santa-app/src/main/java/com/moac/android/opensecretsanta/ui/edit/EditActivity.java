package com.moac.android.opensecretsanta.ui.edit;

import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.inject.base.component.ComponentHolder;
import com.moac.android.opensecretsanta.inject.base.module.BaseActivityModule;
import com.moac.android.opensecretsanta.model.PersistableObject;
import com.moac.android.opensecretsanta.ui.Intents;
import com.moac.android.opensecretsanta.ui.common.BaseEditorActivity;
import com.moac.android.opensecretsanta.ui.common.Saveable;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Host the MemberEditFragment
 */
public class EditActivity extends BaseEditorActivity
        implements ComponentHolder<EditActivityComponent> {

    private static final String FRAGMENT_TAG = MemberEditFragment.class.getName();

    @Nullable
    private EditActivityComponent component;

    @Override
    public void onCreate(final Bundle savedInstance) {
        super.onCreate(savedInstance);
        // Inject dependencies
        component().inject(this);
    }

    protected void createEditorFragment(Bundle savedInstance) {
        if (savedInstance == null) {
            final long memberId = getIntent()
                    .getLongExtra(Intents.MEMBER_ID_INTENT_EXTRA, PersistableObject.UNSET_ID);
            MemberEditFragment fragment = MemberEditFragment.create(memberId);
            getSupportFragmentManager().beginTransaction()
                                       .add(R.id.container_fragment_content, fragment, FRAGMENT_TAG)
                                       .commit();
            mSaveableFragment = fragment;
        } else {
            mSaveableFragment = (Saveable) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        }
    }

    @Override
    public EditActivityComponent component() {
        if (this.component == null) {
            this.component = DaggerEditActivityComponent.builder()
                                                        .openSecretSantaApplicationComponent
                                                                (((OpenSecretSantaApplication) getApplication())
                                                                         .component())
                                                        .baseActivityModule(
                                                                new BaseActivityModule(this))
                                                        .build();
        }
        return this.component;
    }

}
