package com.moac.android.opensecretsanta;

import android.content.Context;
import android.telephony.SmsManager;

import com.moac.android.opensecretsanta.notify.mail.GmailSender;

import dagger.Provides;

@dagger.Module(addsTo = NotifyModule.class,
        overrides = true,
        complete = false)
public class DebugNotifyModule {

    private static final String TAG = NotifyModule.class.getSimpleName();
    private final Context mContext;

    public DebugNotifyModule(Context context) {
        mContext = context;
    }

    @Provides
    SmsManager provideSmsManager() {
        return SmsManager.getDefault();
    }

    @Provides
    GmailSender provideGmailSender() {
        return new GmailSender();
    }

}
