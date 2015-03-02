package com.moac.android.opensecretsanta.module;

import android.content.Context;

import com.moac.android.opensecretsanta.inject.ForApplication;
import com.moac.android.opensecretsanta.instrumentation.DebugInstrumentation;
import com.moac.android.opensecretsanta.instrumentation.Instrumentation;

import dagger.Provides;

@dagger.Module(complete = false, addsTo = AppModule.class)
public final class DebugInstrumentationModule {

    @Provides
    public Instrumentation provideDebugInstrumentation(@ForApplication Context context) {
        return new DebugInstrumentation(context);
    }

}
