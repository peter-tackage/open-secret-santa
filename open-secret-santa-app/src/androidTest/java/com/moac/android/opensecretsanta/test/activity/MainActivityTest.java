package com.moac.android.opensecretsanta.test.activity;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.moac.android.opensecretsanta.activity.MainActivity;

/**
 * Test lifecycle of the MainActivity
 *
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mActivity;
    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        mActivity = getActivity();
    }

    /*
     * Verifies that no exception is thrown when the MainActivity is recreated
     */
    public void test_activityRecreatedOk() throws Throwable {
        // Activity is already created in setUp()
        Log.i("open", Thread.currentThread().toString());

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.recreate();
            }
        });
        getInstrumentation().waitForIdleSync();
    }

}
