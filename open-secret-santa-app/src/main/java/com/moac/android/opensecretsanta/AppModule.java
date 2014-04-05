package com.moac.android.opensecretsanta;

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

@dagger.Module(injects = {OpenSecretSantaApplication.class,
        MainActivity.class, RestrictionsActivity.class, EditActivity.class,
        AssignmentFragment.class, MemberEditFragment.class, MemberListFragment.class, NotifyDialogFragment.class,
        RestrictionsListFragment.class, NotifyExecutorFragment.class,
        SmsSendReceiver.class})
public class AppModule {

    private static final String TAG = AppModule.class.getSimpleName();

    private final OpenSecretSantaApplication mApplication;

    public AppModule(OpenSecretSantaApplication application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    DatabaseManager provideDatabase() {
        DatabaseHelper databaseHelper = new DatabaseHelper(mApplication);
        return new DatabaseManager(databaseHelper);
    }

    @Provides
    @Singleton
    Bus provideBus() {
        return new Bus();
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

}