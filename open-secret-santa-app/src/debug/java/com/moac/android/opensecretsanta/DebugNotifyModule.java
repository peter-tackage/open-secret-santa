package com.moac.android.opensecretsanta;

@dagger.Module(addsTo = NotifyModule.class,
        overrides = true,
        complete = false,
        library = true)
public final class DebugNotifyModule {

    private static final String TAG = NotifyModule.class.getSimpleName();

    // TODO Add mock implementations of transport mechanisms
}
