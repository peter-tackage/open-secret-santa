package com.moac.android.opensecretsanta;

import android.app.Application;
import com.moac.android.opensecretsanta.database.OpenSecretSantaDB;

public class OpenSecretSantaApplication extends Application {

    private static OpenSecretSantaDB mDb = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mDb = new OpenSecretSantaDB(this);
        mDb.open();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mDb.close();
    }

    public static OpenSecretSantaDB getDatabase() {
        return mDb;
    }
}
