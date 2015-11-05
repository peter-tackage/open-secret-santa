package com.moac.android.opensecretsanta.module;

import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.inject.base.ForApplication;

import dagger.Module;
import dagger.Provides;

@Module(includes = {PersistenceModule.class,
                    EventModule.class,
                    NotifyModule.class,
                    AccountModule.class,
                    InstrumentationModule.class})
public final class AppModule {

    private final OpenSecretSantaApplication mApplication;

    public AppModule(OpenSecretSantaApplication application) {
        mApplication = application;
    }

    @ForApplication
    @Provides
    public OpenSecretSantaApplication provideApp() {
        return mApplication;
    }

}