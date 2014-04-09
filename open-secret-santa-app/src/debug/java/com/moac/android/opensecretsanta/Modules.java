package com.moac.android.opensecretsanta;

/**
 * Defines all modules used by this DEBUG build variant
 */
public class Modules {
    static Object[] list(OpenSecretSantaApplication app) {
        return new Object[] {
                new AppModule(app),
                new DebugAppModule()
        };
    }

    private Modules() {
        // No instances.
    }
}
