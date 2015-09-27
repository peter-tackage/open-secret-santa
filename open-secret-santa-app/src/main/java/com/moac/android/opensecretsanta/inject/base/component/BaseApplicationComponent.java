package com.moac.android.opensecretsanta.inject.base.component;

import com.moac.android.opensecretsanta.inject.base.ForApplication;
import com.moac.android.opensecretsanta.inject.base.module.BaseApplicationModule;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = BaseApplicationModule.class)
public interface BaseApplicationComponent {
    Application getApplication();

    @ForApplication
    Context getApplicationContext();

    void inject(Application application);
}
