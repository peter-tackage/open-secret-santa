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
    private final long mDelay;
    private final TimeUnit mUnits;

    public MockSmsTransport(DatabaseManager db, Bus bus, long delay, TimeUnit units) {
        mDb = db;
        mBus = bus;
        mDelay = delay;
        mUnits = units;
    }

    @Override
    public void send(final Assignment _assignment, final Member _giver, String _receiverName, String _groupMsg) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Update Assignment with Sent Status
                _assignment.setSendStatus(isFailure(_giver.getName()) ? Assignment.Status.Failed : Assignment.Status.Sent);
                mDb.update(_assignment);
                mBus.post(new NotifyStatusEvent(_assignment));
            }
        }, TimeUnit.MILLISECONDS.convert(mDelay, mUnits));
    }
    private static boolean isFailure(String giverName) {
        return giverName.toUpperCase().startsWith("FAIL");
    }
}
