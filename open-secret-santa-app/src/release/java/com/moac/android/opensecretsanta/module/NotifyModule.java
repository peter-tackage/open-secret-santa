package com.moac.android.opensecretsanta.module;

import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.inject.base.ForApplication;
import com.moac.android.opensecretsanta.notify.mail.EmailTransporter;
import com.moac.android.opensecretsanta.notify.mail.GmailTransport;
import com.moac.android.opensecretsanta.notify.sms.SmsManagerTransport;
import com.moac.android.opensecretsanta.notify.sms.SmsTransporter;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.SmsManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class NotifyModule {

    private static final String TAG = NotifyModule.class.getSimpleName();

    @Provides
    SmsTransporter provideSmsTransporter(@ForApplication Context context, SharedPreferences prefs) {
        boolean useMultiPartSms = prefs.
                                               getBoolean(context.getString(
                                                       R.string.use_multipart_sms), true);
        return new SmsManagerTransport(context, SmsManager.getDefault(), useMultiPartSms);
    }

    @Provides
    @Singleton
    EmailTransporter provideEmailTransporter() {
        return new GmailTransport();
    }

}