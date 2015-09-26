package com.moac.android.opensecretsanta;

import com.moac.android.opensecretsanta.module.AppModule;

/**
 * Defines all modules used by this RELEASE build variant
 */
public class Modules {
    static Object[] list(OpenSecretSantaApplication app) {
        return new Object[]{
                new AppModule(app)
        };
    }

    private Modules() {
        // No instances.
    }
}
