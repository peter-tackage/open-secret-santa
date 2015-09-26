package com.moac.android.opensecretsanta.test;

import android.util.Log;

import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.test.activity.AbstractOSSActivityUnitTestCase;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;

@Module(injects = {AbstractOSSActivityUnitTestCase.class},
        overrides = false, library = true)
public class TestModule {

    private static final String TAG = TestModule.class.getSimpleName();

    @Provides
    @Singleton
    public DatabaseManager provideMockDatabaseManager() {
        Log.i(TAG, "Providing Mock DatabaseManager");
        return mock(DatabaseManager.class);
    }

    @Provides
    @Singleton
    public Bus provideBus() {
        Log.i(TAG, "Providing Mock Bus");
        return new Bus();
    }

}
