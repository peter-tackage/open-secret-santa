package com.moac.android.opensecretsanta.test.activity;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ActivityUnitTestCase;

import com.moac.android.opensecretsanta.test.MockOSSApplication;

import dagger.ObjectGraph;

public class PatchedActivityInstrumentationTestCase2<T extends Activity> extends ActivityInstrumentationTestCase2<T> {

    public PatchedActivityInstrumentationTestCase2(Class<T> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
    }

}
