package com.moac.android.opensecretsanta.test;

import android.test.mock.MockApplication;

import com.moac.android.inject.dagger.Injector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

public class MockOSSApplication extends MockApplication implements Injector {

    private ObjectGraph mObjectGraph;

    @Override
    public void onCreate() {
        // No super.onCreate() as not supported by MockApplication
        mObjectGraph = ObjectGraph.create(getModules().toArray());
    }

    protected List<Object> getModules() {
        return new ArrayList<Object>(Arrays.asList(new TestModule()));
    }

    @Override
    public ObjectGraph getObjectGraph() {
        return mObjectGraph;
    }

    @Override
    public void inject(Object target) {
        mObjectGraph.inject(this);
    }
}
