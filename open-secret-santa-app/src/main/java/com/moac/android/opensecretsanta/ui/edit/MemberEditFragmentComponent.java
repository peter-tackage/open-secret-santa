package com.moac.android.opensecretsanta.ui.edit;

import com.moac.android.opensecretsanta.inject.base.component.BaseFragmentComponent;
import com.moac.android.opensecretsanta.inject.base.component.FragmentScope;
import com.moac.android.opensecretsanta.inject.base.module.BaseFragmentModule;

import android.support.v4.app.Fragment;

import dagger.Component;

@FragmentScope
@Component(dependencies = EditActivityComponent.class,
        modules = BaseFragmentModule.class)
public interface MemberEditFragmentComponent extends BaseFragmentComponent {

    void inject(MemberEditFragment editFragment);

}
