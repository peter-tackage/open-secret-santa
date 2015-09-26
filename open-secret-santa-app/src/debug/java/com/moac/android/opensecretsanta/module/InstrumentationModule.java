package com.moac.android.opensecretsanta.module;

import android.content.Context;

import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.inject.ForApplication;
import com.moac.android.opensecretsanta.instrumentation.DebugInstrumentation;
import com.moac.android.opensecretsanta.instrumentation.Instrumentation;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(injects = OpenSecretSantaApplication.class,
        complete = false)
public final class InstrumentationModule {

    @Provides
    @Singleton
    public Instrumentation provideDebugInstrumentation(@ForApplication Context context) {
        return new DebugInstrumentation(context);
    }

}
