package com.moac.android.opensecretsanta;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.moac.android.opensecretsanta.activity.EditActivity;
import com.moac.android.opensecretsanta.activity.MainActivity;
import com.moac.android.opensecretsanta.activity.RestrictionsActivity;
import com.moac.android.opensecretsanta.database.DatabaseHelper;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.fragment.MemberEditFragment;
import com.moac.android.opensecretsanta.fragment.MemberListFragment;
import com.moac.android.opensecretsanta.fragment.NotifyExecutorFragment;
import com.moac.android.opensecretsanta.fragment.RestrictionsListFragment;
import com.moac.android.opensecretsanta.notify.sms.receiver.SmsManagerSendReceiver;

import javax.inject.Singleton;

import dagger.Provides;

@dagger.Module(
        injects = {
                OpenSecretSantaApplication.class,
                MainActivity.class,
                EditActivity.class,
                RestrictionsActivity.class,
                MemberEditFragment.class,
                MemberListFragment.class,
                NotifyExecutorFragment.class,
                RestrictionsListFragment.class,
                SmsManagerSendReceiver.class},
        complete = false)
public final class PersistenceModule {

    private static final String TAG = PersistenceModule.class.getSimpleName();

    @Provides
    @Singleton
    DatabaseManager provideDatabase(Application app) {
        DatabaseHelper databaseHelper = new DatabaseHelper(app);
        return new DatabaseManager(databaseHelper);
    }

    @Provides
    @Singleton
    SharedPreferences provideDefaultSharedPreferences(Application app) {
        return  PreferenceManager.getDefaultSharedPreferences(app);
    }

}