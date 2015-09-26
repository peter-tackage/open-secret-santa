package com.moac.android.opensecretsanta.module;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.activity.EditActivity;
import com.moac.android.opensecretsanta.activity.MainActivity;
import com.moac.android.opensecretsanta.activity.RestrictionsActivity;
import com.moac.android.opensecretsanta.database.DatabaseHelper;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.fragment.MemberEditFragment;
import com.moac.android.opensecretsanta.fragment.MemberListFragment;
import com.moac.android.opensecretsanta.fragment.NotifyExecutorFragment;
import com.moac.android.opensecretsanta.fragment.RestrictionsListFragment;
import com.moac.android.opensecretsanta.inject.ForApplication;
import com.moac.android.opensecretsanta.notify.sms.SmsManagerSendReceiver;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
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
    DatabaseManager provideDatabase(@ForApplication Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        return new DatabaseManager(databaseHelper);
    }

    @Provides
    @Singleton
    SharedPreferences provideDefaultSharedPreferences(@ForApplication Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

}