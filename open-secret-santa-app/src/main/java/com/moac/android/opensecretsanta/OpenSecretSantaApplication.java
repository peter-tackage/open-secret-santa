package com.moac.android.opensecretsanta;

import android.app.Application;
import com.moac.android.opensecretsanta.database.DatabaseHelper;
import com.moac.android.opensecretsanta.database.DatabaseManager;

public class OpenSecretSantaApplication extends Application {

    private static DatabaseManager mDatabaseManager = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabaseManager = initDatabase();
    }

    public static DatabaseManager getDatabase() {
        return mDatabaseManager;
    }

    private DatabaseManager initDatabase() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        return new DatabaseManager(databaseHelper);
    }
}
