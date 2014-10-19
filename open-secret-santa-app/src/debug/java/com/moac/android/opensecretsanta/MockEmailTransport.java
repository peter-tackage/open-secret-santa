package com.moac.android.opensecretsanta;

import com.moac.android.opensecretsanta.notify.NotificationFailureException;
import com.moac.android.opensecretsanta.notify.mail.EmailTransporter;

import java.util.concurrent.TimeUnit;

public class MockEmailTransport implements EmailTransporter {

    private static final String TAG = MockEmailTransport.class.getSimpleName();

    private final long mDelay;
    private final TimeUnit mUnits;

    public MockEmailTransport(long delay, TimeUnit units) {
        mDelay = delay;
        mUnits = units;
    }

    @Override
    public void send(String subject, String body, String senderAddress, String toAddress, String oauthToken) throws NotificationFailureException {
        try {
            Thread.sleep(TimeUnit.MILLISECONDS.convert(mDelay, mUnits));
        } catch (InterruptedException e) {
            // Ignore
        }
        if (isFailure(toAddress)) throw new NotificationFailureException("Mock Failure");
    }

    private static boolean isFailure(String emailAddress) {
        return emailAddress.toUpperCase().startsWith("FAIL");
    }
}
