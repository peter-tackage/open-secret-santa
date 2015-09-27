package com.moac.android.opensecretsanta.ui.restrictions;

import com.moac.android.opensecretsanta.inject.base.component.BaseFragmentComponent;
import com.moac.android.opensecretsanta.inject.base.component.FragmentScope;
import com.moac.android.opensecretsanta.inject.base.module.BaseFragmentModule;

import android.support.v4.app.Fragment;

import dagger.Component;

@FragmentScope
@Component(dependencies = RestrictionsActivityComponent.class,
        modules = BaseFragmentModule.class)
public interface RestrictionsListFragmentComponent extends BaseFragmentComponent {

    void inject(RestrictionsListFragment restrictionsListFragment);

}
