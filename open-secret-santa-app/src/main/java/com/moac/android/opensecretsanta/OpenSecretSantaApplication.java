package com.moac.android.opensecretsanta;

import android.app.Application;
import com.moac.android.opensecretsanta.database.DatabaseHelper;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.ContactMethod;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.Restriction;
import com.moac.android.opensecretsanta.util.Utils;

public class OpenSecretSantaApplication extends Application {

    private static final String TAG = "OpenSecretSantaApp";

    private static OpenSecretSantaApplication sInstance;
    private DatabaseManager mDatabaseManager;

    public OpenSecretSantaApplication() {
        super();
        if(sInstance == null) {
            sInstance = this; // init self singleton.
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabaseManager = initDatabase();
        Utils.doOnce(getApplicationContext(), "initTestData", new Runnable() {
            @Override
            public void run() {
                // (On upgrade) Don't add another group if there
                // is at least one already
                if(!mDatabaseManager.queryHasGroup()) {
                    createFirstDraw();
                }
            }
        });
    }

    public static OpenSecretSantaApplication getInstance() {
        return sInstance;
    }

    public DatabaseManager getDatabase() {
        return mDatabaseManager;
    }

    private DatabaseManager initDatabase() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        return new DatabaseManager(databaseHelper);
    }

    private void createFirstDraw() {
        Group group1 = new Group();
        group1.setName(getString(R.string.first_group_name));
        mDatabaseManager.create(group1);
    }
}
