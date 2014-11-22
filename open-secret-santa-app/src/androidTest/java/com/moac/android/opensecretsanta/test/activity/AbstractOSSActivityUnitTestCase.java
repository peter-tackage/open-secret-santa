package com.moac.android.opensecretsanta.test.activity;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityUnitTestCase;

import com.moac.android.inject.dagger.Injector;
import com.moac.android.opensecretsanta.test.MockOSSApplication;
import com.moac.android.opensecretsanta.test.TestModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

public abstract class AbstractOSSActivityUnitTestCase<T extends Activity> extends ActivityUnitTestCase<T> {

    protected T mActivity;
    private Class<T> mActivityClass;
    private ObjectGraph mObjectGraph;

    public AbstractOSSActivityUnitTestCase(Class<T> activityClass) {
        super(activityClass);
        mActivityClass = activityClass;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());

        // The application provides the injections to the Activity
        MockOSSApplication mockApplication = new MockOSSApplication();
        mockApplication.onCreate();

        // Add in any modules specific for the test
        mObjectGraph = mockApplication.getObjectGraph();

        // Inject into the testcase
        mObjectGraph.inject(this);

        // Set the application
        setApplication(mockApplication);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        mActivity = null;
        mObjectGraph = null;
    }

    protected void startActivity(Extra extra) throws Exception {
        Intent intent = new Intent(getInstrumentation().getTargetContext(),
                mActivityClass);
        if (extra != null) {
            intent.putExtra(extra.key, extra.data);
        }
        mActivity = startActivity(intent, null, null);
    }

    protected List<Object> getModules() {
        return new ArrayList<Object>();
    }

}
