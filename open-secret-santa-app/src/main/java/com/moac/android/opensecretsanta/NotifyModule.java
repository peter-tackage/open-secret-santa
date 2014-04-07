package com.moac.android.opensecretsanta;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.SharedPreferences;
import android.telephony.SmsManager;

import com.moac.android.opensecretsanta.fragment.NotifyDialogFragment;
import com.moac.android.opensecretsanta.fragment.NotifyExecutorFragment;
import com.moac.android.opensecretsanta.notify.mail.EmailTransporter;
import com.moac.android.opensecretsanta.notify.mail.GmailTransport;
import com.moac.android.opensecretsanta.notify.sms.SmsManagerTransport;
import com.moac.android.opensecretsanta.notify.sms.SmsTransporter;

import javax.inject.Singleton;

import dagger.Provides;

@dagger.Module(
        injects = {
                NotifyExecutorFragment.class,
                NotifyDialogFragment.class},
        complete = false,
        library = true)
public final class NotifyModule {

    private static final String TAG = NotifyModule.class.getSimpleName();

    @Provides
    SmsManager provideSmsManager() {
        return SmsManager.getDefault();
    }

    @Provides
    SmsTransporter provideSmsTransporter(Application app, SmsManager smsManager, SharedPreferences prefs) {
        boolean useMultiPartSms = prefs.
                getBoolean(app.getString(R.string.use_multipart_sms), true);
        return new SmsManagerTransport(app, smsManager, useMultiPartSms);
    }

    @Provides
    EmailTransporter provideEmailTransporter() {
        return new GmailTransport();
    }

    @Provides
    @Singleton
    AccountManager provideAccountManager(Application app) {
        return AccountManager.get(app);
    }

}