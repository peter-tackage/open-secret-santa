package com.moac.android.opensecretsanta.module;

import com.moac.android.opensecretsanta.MockEmailTransport;
import com.moac.android.opensecretsanta.MockSmsTransport;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.notify.mail.EmailTransporter;
import com.moac.android.opensecretsanta.notify.sms.SmsTransporter;
import com.squareup.otto.Bus;

import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;

@Module
public final class NotifyModule {

    private static final String TAG = NotifyModule.class.getSimpleName();

    @Provides
    public EmailTransporter provideMockEmailTransporter() {
        return new MockEmailTransport(3, TimeUnit.SECONDS);
    }

    @Provides
    public SmsTransporter provideMockSmsTransporter(DatabaseManager db, Bus bus) {
        return new MockSmsTransport(db, bus, 5, TimeUnit.SECONDS);
    }
}
