package com.moac.android.opensecretsanta;

import com.moac.android.opensecretsanta.notify.NotificationFailureException;
import com.moac.android.opensecretsanta.notify.mail.EmailTransporter;

import java.util.concurrent.TimeUnit;

public class MockEmailTransport implements EmailTransporter {

    private static final String TAG = MockEmailTransport.class.getSimpleName();

    private final boolean mDoSucceed;
    private final long mDelay;
    private final TimeUnit mUnits;

    public MockEmailTransport(boolean doSucceed, long delay, TimeUnit units) {
        mDoSucceed = doSucceed;
        mDelay = delay;
        mUnits = units;
    }
    @Override
    public void send(String subject, String body, String user, String oauthToken, String recipients) throws NotificationFailureException {
        try {
            Thread.sleep(TimeUnit.MILLISECONDS.convert(mDelay, mUnits));
        } catch (InterruptedException e) {
            // Ignore
        }
        if(!mDoSucceed) throw new  NotificationFailureException("Mock Failure");
    }
}
