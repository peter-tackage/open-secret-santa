package com.moac.android.opensecretsanta.inject.base.component;

import com.moac.android.opensecretsanta.inject.base.ForActivity;
import com.moac.android.opensecretsanta.inject.base.module.BaseFragmentModule;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import dagger.Component;

@FragmentScope
@Component(dependencies = BaseActivityComponent.class, modules = BaseFragmentModule.class)
public interface BaseFragmentComponent {

    @FragmentScope
    Fragment getFragment();

//    @FragmentScope
//    @ForActivity
//    Activity getActivity();
//
//    @FragmentScope
//    @ForActivity
//    Context getActivityContext();
}
