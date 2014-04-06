package com.moac.android.opensecretsanta;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.moac.android.opensecretsanta.activity.EditActivity;
import com.moac.android.opensecretsanta.activity.MainActivity;
import com.moac.android.opensecretsanta.activity.RestrictionsActivity;
import com.moac.android.opensecretsanta.database.DatabaseHelper;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.fragment.AssignmentFragment;
import com.moac.android.opensecretsanta.fragment.MemberEditFragment;
import com.moac.android.opensecretsanta.fragment.MemberListFragment;
import com.moac.android.opensecretsanta.fragment.NotifyDialogFragment;
import com.moac.android.opensecretsanta.fragment.NotifyExecutorFragment;
import com.moac.android.opensecretsanta.fragment.RestrictionsListFragment;
import com.moac.android.opensecretsanta.notify.receiver.SmsSendReceiver;

import javax.inject.Singleton;

import dagger.Provides;

@dagger.Module(injects = {OpenSecretSantaApplication.class,
        MainActivity.class, RestrictionsActivity.class, EditActivity.class,
        AssignmentFragment.class, MemberEditFragment.class, MemberListFragment.class, NotifyDialogFragment.class,
        RestrictionsListFragment.class, NotifyExecutorFragment.class,
        SmsSendReceiver.class}, complete = false)
public class PersistenceModule {

    private static final String TAG = PersistenceModule.class.getSimpleName();

    private final OpenSecretSantaApplication mApplication;

    public PersistenceModule(OpenSecretSantaApplication application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    DatabaseManager provideDatabase() {
        DatabaseHelper databaseHelper = new DatabaseHelper(mApplication);
        return new DatabaseManager(databaseHelper);
    }

    @Provides
    @Singleton
    SharedPreferences provideDefaultSharedPreferences() {
        return  PreferenceManager.getDefaultSharedPreferences(mApplication);
    }

}