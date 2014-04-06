package com.moac.android.opensecretsanta;

import android.accounts.AccountManager;
import android.content.Context;
import android.telephony.SmsManager;

import com.moac.android.opensecretsanta.activity.EditActivity;
import com.moac.android.opensecretsanta.activity.MainActivity;
import com.moac.android.opensecretsanta.activity.RestrictionsActivity;
import com.moac.android.opensecretsanta.database.DatabaseHelper;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.fragment.AssignmentFragment;
import com.moac.android.opensecretsanta.fragment.MemberEditFragment;
import com.moac.android.opensecretsanta.fragment.MemberListFragment;
import com.moac.android.opensecretsanta.fragment.NotifyDialogFragment;
import com.moac.android.opensecretsanta.fragment.NotifyExecutorFragment;
import com.moac.android.opensecretsanta.fragment.RestrictionsListFragment;
import com.moac.android.opensecretsanta.notify.mail.GmailSender;
import com.moac.android.opensecretsanta.notify.receiver.SmsSendReceiver;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Provides;

@dagger.Module(complete= false, injects = {NotifyExecutorFragment.class, NotifyDialogFragment.class})
public class NotifyModule {

    private static final String TAG = NotifyModule.class.getSimpleName();
    private final Context mContext;

    public NotifyModule(Context context) {
        mContext = context;
    }

    @Provides
    @Singleton
    SmsManager provideSmsManager() {
        return SmsManager.getDefault();
    }

    @Provides
    @Singleton
    GmailSender provideGmailSender() {
        return new GmailSender();
    }

    @Provides
    @Singleton
    AccountManager provideAccountManager() {
        return AccountManager.get(mContext);
    }

}