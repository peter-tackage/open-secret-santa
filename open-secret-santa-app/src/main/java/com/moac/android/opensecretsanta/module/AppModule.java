package com.moac.android.opensecretsanta.module;

import android.content.Context;

import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.inject.ForApplication;

import dagger.Provides;

@dagger.Module(injects = {OpenSecretSantaApplication.class},
        includes = {PersistenceModule.class, EventModule.class, NotifyModule.class})
public final class AppModule {

    private static final String TAG = AppModule.class.getSimpleName();

    private final OpenSecretSantaApplication mApplication;

    public AppModule(OpenSecretSantaApplication application) {
        mApplication = application;
    }

    @ForApplication
    @Provides
    public Context provideApplicationContext() {
        return mApplication.getApplicationContext();
    }

    // I don't like use the InjectingApplicationModule as it requires complete=false here, thus
    // disabling many of the compile time checks
}