package com.moac.android.opensecretsanta.module;

import android.content.Context;

import com.moac.android.opensecretsanta.inject.base.ForApplication;
import com.moac.android.opensecretsanta.instrumentation.DebugInstrumentation;
import com.moac.android.opensecretsanta.instrumentation.Instrumentation;

import dagger.Module;
import dagger.Provides;

@Module
public final class InstrumentationModule {

    @Provides
    public Instrumentation provideDebugInstrumentation(@ForApplication Context context) {
        return new DebugInstrumentation(context);
    }

}
