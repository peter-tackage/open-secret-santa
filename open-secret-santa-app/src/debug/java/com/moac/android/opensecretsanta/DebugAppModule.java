package com.moac.android.opensecretsanta;

/*
 * A shell module that indirectly provides debug override instances of AppModule injections
 */
@dagger.Module(addsTo = AppModule.class,
        includes = {DebugNotifyModule.class},
        overrides = true,
        library = true)
public class DebugAppModule {
}
