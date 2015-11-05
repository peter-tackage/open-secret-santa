package com.moac.android.opensecretsanta.inject.base.module;

import com.moac.android.opensecretsanta.inject.base.ForApplication;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class BaseApplicationModule {

    private final Application application;

    public BaseApplicationModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton @ForApplication
    public Context provideApplicationContext() {
        return application.getApplicationContext();
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return application;
    }

}
