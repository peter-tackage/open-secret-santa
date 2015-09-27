package com.moac.android.opensecretsanta.ui.restrictions;

import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.inject.base.component.ComponentHolder;
import com.moac.android.opensecretsanta.inject.base.module.BaseActivityModule;
import com.moac.android.opensecretsanta.model.PersistableObject;
import com.moac.android.opensecretsanta.ui.Intents;
import com.moac.android.opensecretsanta.ui.common.BaseEditorActivity;
import com.moac.android.opensecretsanta.ui.common.Saveable;
import com.moac.android.opensecretsanta.util.Preconditions;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Host the RestrictionsListFragment
 */
public class RestrictionsActivity extends BaseEditorActivity
        implements ComponentHolder<RestrictionsActivityComponent> {

    private static final String FRAGMENT_TAG = RestrictionsListFragment.class.getName();

    @Nullable
    private RestrictionsActivityComponent component;

    @Override
    public void onCreate(final Bundle savedInstance) {
        super.onCreate(savedInstance);
        // Inject dependencies
        component().inject(this);
    }

    @Override
    protected void createEditorFragment(Bundle savedInstance) {
        if (savedInstance == null) {
            // Add the restrictions list fragment
            final long groupId = getIntent()
                    .getLongExtra(Intents.GROUP_ID_INTENT_EXTRA, PersistableObject.UNSET_ID);
            final long memberId = getIntent()
                    .getLongExtra(Intents.MEMBER_ID_INTENT_EXTRA, PersistableObject.UNSET_ID);
            RestrictionsListFragment fragment = RestrictionsListFragment.create(groupId, memberId);
            getSupportFragmentManager().beginTransaction()
                                       .add(R.id.container_fragment_content, fragment, FRAGMENT_TAG)
                                       .commit();
            mSaveableFragment = fragment;
        } else {
            mSaveableFragment = (Saveable) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        }
    }

    @Override
    public RestrictionsActivityComponent component() {
        if(this.component == null) {
            this.component = DaggerRestrictionsActivityComponent.builder()
                                                                .openSecretSantaApplicationComponent
                                                                        (((OpenSecretSantaApplication) getApplication())
                                                                                 .component())
                                                                .baseActivityModule(
                                                                        new BaseActivityModule(
                                                                                this))
                                                                .build();
        }
        return this.component;
    }

}
