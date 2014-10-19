package com.moac.android.opensecretsanta.module;

import com.moac.android.opensecretsanta.fragment.MemberListFragment;
import com.moac.android.opensecretsanta.fragment.NotifyExecutorFragment;
import com.moac.android.opensecretsanta.notify.sms.SmsManagerSendReceiver;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Provides;

@dagger.Module(
        injects = {
                MemberListFragment.class,
                NotifyExecutorFragment.class,
                SmsManagerSendReceiver.class
        }, complete = false)

// Complete = false because this module cannot provide all the dependencies for all the injection points
public final class EventModule {

    private static final String TAG = EventModule.class.getSimpleName();

    @Provides
    @Singleton
    Bus provideBus() {
        return new Bus();
    }

}