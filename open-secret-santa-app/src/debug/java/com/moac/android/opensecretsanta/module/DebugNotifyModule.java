package com.moac.android.opensecretsanta.module;

import android.telephony.SmsManager;

import com.moac.android.opensecretsanta.MockEmailTransport;
import com.moac.android.opensecretsanta.MockSmsTransport;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.notify.mail.EmailTransporter;
import com.moac.android.opensecretsanta.notify.sms.SmsTransporter;
import com.squareup.otto.Bus;

import java.util.concurrent.TimeUnit;

import dagger.Provides;

@dagger.Module(
        addsTo = NotifyModule.class,
        overrides = true,
        complete = false)
public final class DebugNotifyModule {

    private static final String TAG = DebugNotifyModule.class.getSimpleName();

    @Provides
    public EmailTransporter provideMockEmailTransporter() {
        return new MockEmailTransport(3, TimeUnit.SECONDS);
    }

    @Provides
    public SmsTransporter provideMockSmsTransporter(DatabaseManager db, Bus bus) {
        return new MockSmsTransport(db, bus, 5, TimeUnit.SECONDS);
    }
}
