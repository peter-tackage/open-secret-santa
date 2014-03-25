package com.moac.android.opensecretsanta.test.activity;

import android.test.ActivityInstrumentationTestCase2;

import com.moac.android.opensecretsanta.AppModule;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.activity.MainActivity;
import com.moac.android.opensecretsanta.activity.RestrictionsActivity;

/**
 * Test lifecycle of the MainActivity
 *
 */
public class RestrictionsActivityTest extends ActivityInstrumentationTestCase2<RestrictionsActivity> {

    private RestrictionsActivity mActivity;

    public RestrictionsActivityTest() {
        super(RestrictionsActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        mActivity = getActivity();
    }

    /*
     * Verifies that no exception is thrown when the RestrictionsActivity is recreated
     */
    public void test_activityRecreatedOk() {
        // Activity is already created in setUp()

        // End the activity
        mActivity.finish();

        // Recreate this activity
        mActivity = this.getActivity();
    }

    public class TestModule extends AppModule {

        public TestModule(OpenSecretSantaApplication application) {
            super(application);
        }
    }

}
