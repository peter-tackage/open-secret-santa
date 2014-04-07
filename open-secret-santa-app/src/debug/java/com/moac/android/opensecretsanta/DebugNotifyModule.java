package com.moac.android.opensecretsanta;

import com.moac.android.opensecretsanta.notify.mail.EmailTransporter;

import java.util.concurrent.TimeUnit;

import dagger.Provides;

@dagger.Module(addsTo = NotifyModule.class,
        overrides = true,
        complete = false,
        library = true)
public final class DebugNotifyModule {

    private static final String TAG = NotifyModule.class.getSimpleName();

    @Provides
    public EmailTransporter provideMockEmailTransporter() {
        return new MockEmailTransport(false, 3, TimeUnit.SECONDS);
    }
}
