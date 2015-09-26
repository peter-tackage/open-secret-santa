package com.moac.android.opensecretsanta.module;

import android.content.Context;

import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.inject.ForApplication;

import dagger.Module;
import dagger.Provides;

@Module(includes = {PersistenceModule.class, EventModule.class, NotifyModule.class, InstrumentationModule.class},
        injects = OpenSecretSantaApplication.class)
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

}
