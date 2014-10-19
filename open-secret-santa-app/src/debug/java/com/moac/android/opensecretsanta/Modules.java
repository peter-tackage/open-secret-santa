package com.moac.android.opensecretsanta;

import com.moac.android.opensecretsanta.module.AppModule;
import com.moac.android.opensecretsanta.module.DebugNotifyModule;

/**
 * Defines all modules used by this DEBUG build variant
 */
public class Modules {
    static Object[] list(OpenSecretSantaApplication app) {
        return new Object[] {
                new AppModule(app),
                new DebugNotifyModule()
        };
    }

    private Modules() {
        // No instances.
    }
}
