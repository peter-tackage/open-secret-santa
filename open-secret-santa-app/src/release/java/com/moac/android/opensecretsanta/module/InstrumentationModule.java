package com.moac.android.opensecretsanta.module;

import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.instrumentation.Instrumentation;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(injects = OpenSecretSantaApplication.class,
        complete = false)
public class InstrumentationModule {

    @Provides
    @Singleton
    public Instrumentation provideReleaseInstrumentation() {
        return new Instrumentation() {
            @Override
            public void init() {
                // Do nothing in release build
            }
        };
    }
}
