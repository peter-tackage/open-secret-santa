package com.moac.android.opensecretsanta.module;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.SmsManager;

import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.fragment.NotifyDialogFragment;
import com.moac.android.opensecretsanta.fragment.NotifyExecutorFragment;
import com.moac.android.opensecretsanta.inject.ForApplication;
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
        complete = false)
public final class NotifyModule {

    private static final String TAG = NotifyModule.class.getSimpleName();

    @Provides
    SmsTransporter provideSmsTransporter(@ForApplication Context context, SharedPreferences prefs) {
        boolean useMultiPartSms = prefs.
                getBoolean(context.getString(R.string.use_multipart_sms), true);
        return new SmsManagerTransport(context, SmsManager.getDefault(), useMultiPartSms);
    }

    @Provides
    @Singleton
    EmailTransporter provideEmailTransporter() {
        return new GmailTransport();
    }

    @Provides
    @Singleton
    AccountManager provideAccountManager(@ForApplication Context context) {
        return AccountManager.get(context);
    }

}