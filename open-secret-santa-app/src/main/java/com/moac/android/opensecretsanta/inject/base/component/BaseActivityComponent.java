package com.moac.android.opensecretsanta.inject.base.component;

import com.moac.android.opensecretsanta.inject.base.ForActivity;
import com.moac.android.opensecretsanta.inject.base.module.BaseActivityModule;

import android.app.Activity;
import android.content.Context;

import dagger.Component;

@ActivityScope
@Component(dependencies = BaseApplicationComponent.class, modules = BaseActivityModule.class)
public interface BaseActivityComponent {
    Activity getActivity();

    @ForActivity
    Context getActivityContext();
}
