package com.moac.android.opensecretsanta.module;

import com.moac.android.opensecretsanta.database.DatabaseHelper;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.inject.base.ForApplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module()
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