package com.moac.android.opensecretsanta.module;

import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.instrumentation.Instrumentation;

import dagger.Provides;

@dagger.Module(addsTo = AppModule.class)
public class InstrumentationModule {

    @Provides
    public Instrumentation provideReleaseInstrumentation() {
        return new Instrumentation() {
            @Override
            public void init() {
                // Do nothing in release build
            }
        };
    }
}
