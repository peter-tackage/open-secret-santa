package com.moac.android.opensecretsanta;

import android.os.Handler;
import android.os.Looper;

import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.notify.NotifyStatusEvent;
import com.moac.android.opensecretsanta.notify.sms.SmsTransporter;
import com.squareup.otto.Bus;

import java.util.concurrent.TimeUnit;

public class MockSmsTransport implements SmsTransporter {

    private static final String TAG = MockSmsTransport.class.getSimpleName();

    private DatabaseManager mDb;
    private final Bus mBus;
    private final boolean mDoSucceed;
    private final long mDelay;
    private final TimeUnit mUnits;

    public MockSmsTransport(DatabaseManager db, Bus bus, boolean doSucceed, long delay, TimeUnit units) {
        mDb = db;
        mBus = bus;
        mDoSucceed = doSucceed;
        mDelay = delay;
        mUnits = units;
    }

    @Override
    public void send(final Assignment _assignment, Member _giver, String _receiverName, String _groupMsg) {

        try {
            Thread.sleep(TimeUnit.MILLISECONDS.convert(mDelay, mUnits));
        } catch (InterruptedException e) {
            // Ignore
        }

        // Update Assignment with Sent Status
        _assignment.setSendStatus(mDoSucceed ? Assignment.Status.Sent : Assignment.Status.Failed);

        mDb.update(_assignment);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mBus.post(new NotifyStatusEvent(_assignment));
            }
        });
    }
}
