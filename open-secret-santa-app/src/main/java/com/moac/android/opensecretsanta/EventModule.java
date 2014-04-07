package com.moac.android.opensecretsanta;

import com.moac.android.opensecretsanta.fragment.MemberListFragment;
import com.moac.android.opensecretsanta.fragment.NotifyExecutorFragment;
import com.moac.android.opensecretsanta.notify.receiver.SmsSendReceiver;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Provides;

@dagger.Module(
        injects = {
                MemberListFragment.class,
                NotifyExecutorFragment.class,
                SmsSendReceiver.class
        },
        complete = false)
public class EventModule {

    private static final String TAG = EventModule.class.getSimpleName();

    @Provides
    @Singleton
    Bus provideBus() {
        return new Bus();
    }

}