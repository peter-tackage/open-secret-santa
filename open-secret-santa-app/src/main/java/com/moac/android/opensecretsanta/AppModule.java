package com.moac.android.opensecretsanta;

@dagger.Module(injects = {OpenSecretSantaApplication.class},
        includes = {NotifyModule.class, PersistenceModule.class, EventModule.class},
        complete = false
)
public final class AppModule {

    private static final String TAG = AppModule.class.getSimpleName();

    private final OpenSecretSantaApplication mApplication;

    public AppModule(OpenSecretSantaApplication application) {
        mApplication = application;
    }

    // Most injections at this scope are provided by the InjectingApplicationModule
}