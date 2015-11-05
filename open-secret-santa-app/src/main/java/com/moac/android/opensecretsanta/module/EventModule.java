package com.moac.android.opensecretsanta.module;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class EventModule {

    private static final String TAG = EventModule.class.getSimpleName();

    @Provides
    @Singleton
    Bus provideBus() {
        return new Bus();
    }

}