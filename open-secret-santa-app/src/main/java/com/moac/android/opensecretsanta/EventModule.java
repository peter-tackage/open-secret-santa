package com.moac.android.opensecretsanta;

import com.moac.android.opensecretsanta.fragment.MemberListFragment;
import com.moac.android.opensecretsanta.fragment.NotifyExecutorFragment;
import com.moac.android.opensecretsanta.notify.sms.receiver.SmsManagerSendReceiver;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Provides;

@dagger.Module(
        injects = {
                MemberListFragment.class,
                NotifyExecutorFragment.class,
                SmsManagerSendReceiver.class
        },
        complete = false)
public final class EventModule {

    private static final String TAG = EventModule.class.getSimpleName();

    @Provides
    @Singleton
    Bus provideBus() {
        return new Bus();
    }

}