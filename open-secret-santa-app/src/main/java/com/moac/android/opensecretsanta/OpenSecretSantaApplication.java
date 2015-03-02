package com.moac.android.opensecretsanta;

import android.content.SharedPreferences;

import com.moac.android.inject.dagger.InjectingApplication;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.instrumentation.Instrumentation;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.util.GroupUtils;
import com.moac.android.opensecretsanta.util.Utils;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class OpenSecretSantaApplication extends InjectingApplication {

    public static final String MOST_RECENT_GROUP_KEY = "most_recent_group_id";

    private static final String CREATE_DEFAULT_GROUP_TASK = "createDefaultGroup";
    private static final String TAG = "OpenSecretSantaApplication";

    @Inject
    DatabaseManager mDatabaseManager;

    @Inject
    SharedPreferences mSharedPreferences;

    @Inject
    Instrumentation mInstrumentation;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize app instrumentation
        mInstrumentation.init();

        // Ensure a default group exists
        initializeDefaultGroup();
    }

    private void initializeDefaultGroup() {
        Utils.doOnce(mSharedPreferences,
                CREATE_DEFAULT_GROUP_TASK, new Runnable() {
                    @Override
                    public void run() {
                        // Don't add another group if there is at least one already
                        if (!mDatabaseManager.queryHasGroup()) {
                            createDefaultInitialGroup();
                        }
                    }
                });
    }

    private void createDefaultInitialGroup() {
        String baseName = getString(R.string.base_group_name);
        Group myFirstGroup = GroupUtils.createIncrementingGroup(mDatabaseManager, baseName);
        // Assign as the current Group
        mSharedPreferences.edit().
                putLong(MOST_RECENT_GROUP_KEY, myFirstGroup.getId()).apply();
    }

    @Override
    public List<Object> getModules() {
        List<Object> modules = super.getModules();
        modules.addAll(Arrays.asList(Modules.list(this)));
        return modules;
    }

}
