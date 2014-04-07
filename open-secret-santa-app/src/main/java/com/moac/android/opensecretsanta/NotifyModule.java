package com.moac.android.opensecretsanta;

import android.accounts.AccountManager;
import android.content.Context;
import android.telephony.SmsManager;

import com.moac.android.opensecretsanta.fragment.NotifyDialogFragment;
import com.moac.android.opensecretsanta.fragment.NotifyExecutorFragment;
import com.moac.android.opensecretsanta.notify.mail.GmailSender;

import javax.inject.Singleton;

import dagger.Provides;

@dagger.Module(
        injects = {
                NotifyExecutorFragment.class,
                NotifyDialogFragment.class},
        complete = false)
public class NotifyModule {

    private static final String TAG = NotifyModule.class.getSimpleName();
    private final Context mContext;

    public NotifyModule(Context context) {
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

    @Provides
    @Singleton
    AccountManager provideAccountManager() {
        return AccountManager.get(mContext);
    }

}