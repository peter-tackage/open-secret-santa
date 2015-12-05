package com.moac.android.opensecretsanta.inject.base.component;

import android.support.v4.app.Fragment;

import com.moac.android.opensecretsanta.inject.base.module.BaseFragmentModule;

import dagger.Component;

@FragmentScope
@Component(dependencies = BaseActivityComponent.class, modules = BaseFragmentModule.class)
public interface BaseFragmentComponent {

    @FragmentScope
    Fragment getFragment();

}
