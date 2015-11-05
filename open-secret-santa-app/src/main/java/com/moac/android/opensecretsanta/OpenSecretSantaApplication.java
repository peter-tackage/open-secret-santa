package com.moac.android.opensecretsanta;

import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.inject.base.component.ComponentHolder;
import com.moac.android.opensecretsanta.inject.base.module.BaseApplicationModule;
import com.moac.android.opensecretsanta.instrumentation.Instrumentation;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.module.AppModule;
import com.moac.android.opensecretsanta.util.GroupUtils;
import com.moac.android.opensecretsanta.util.Utils;

import android.app.Application;
import android.content.SharedPreferences;

import javax.inject.Inject;

public class OpenSecretSantaApplication extends Application implements
                                                            ComponentHolder<OpenSecretSantaApplicationComponent> {

    public static final String MOST_RECENT_GROUP_KEY = "most_recent_group_id";

    private static final String CREATE_DEFAULT_GROUP_TASK = "createDefaultGroup";
    private static final String TAG = "OpenSecretSantaApplication";

    @Inject
    DatabaseManager databaseManager;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    Instrumentation instrumentation;

    private OpenSecretSantaApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        // Inject dependencies
        component().inject(this);

        // Initialize app instrumentation
        instrumentation.init();

        // Ensure a default group exists
        initializeDefaultGroup();
    }

    private void initializeDefaultGroup() {
        Utils.doOnce(sharedPreferences,
                     CREATE_DEFAULT_GROUP_TASK, new Runnable() {
                    @Override
                    public void run() {
                        // Don't add another group if there is at least one already
                        if (!databaseManager.queryHasGroup()) {
                            createDefaultInitialGroup();
                        }
                    }
                });
    }

    private void createDefaultInitialGroup() {
        String baseName = getString(R.string.base_group_name);
        Group myFirstGroup = GroupUtils.createIncrementingGroup(databaseManager, baseName);
        // Assign as the current Group
        sharedPreferences.edit().
                putLong(MOST_RECENT_GROUP_KEY, myFirstGroup.getId()).apply();
    }

    @Override
    public OpenSecretSantaApplicationComponent component() {
        if (this.component == null) {
            this.component =
                    DaggerOpenSecretSantaApplicationComponent.builder()
                                                             .baseApplicationModule(
                                                                     new BaseApplicationModule(
                                                                             this))
                                                             .appModule(new AppModule(this))
                                                             .build();
        }
        return this.component;
    }

}
