package com.moac.android.opensecretsanta;

import android.app.Application;
import android.preference.PreferenceManager;
import com.moac.android.opensecretsanta.database.DatabaseHelper;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.util.GroupUtils;
import com.moac.android.opensecretsanta.util.Utils;

public class OpenSecretSantaApplication extends Application {

    public static final String MOST_RECENT_GROUP_KEY = "most_recent_group_id";
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
        Utils.doOnce(getApplicationContext(), "createDefaultGroup", new Runnable() {
            @Override
            public void run() {
                // Don't add another group if there is at least one already
                if(!mDatabaseManager.queryHasGroup()) {
                    createDefaultInitialGroup();
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

    private void createDefaultInitialGroup() {
        String baseName = getString(R.string.base_group_name);
        Group group1 = GroupUtils.createIncrementingGroup(mDatabaseManager, baseName);
        // Assign as the current Group
        PreferenceManager.getDefaultSharedPreferences(this).edit().
          putLong(MOST_RECENT_GROUP_KEY, group1.getId()).apply();
    }
}
