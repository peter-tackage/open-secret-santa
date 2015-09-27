package com.moac.android.opensecretsanta.notify.sms;

import com.moac.android.opensecretsanta.OpenSecretSantaApplicationComponent;
import com.moac.android.opensecretsanta.inject.base.component.BroadcastReceiverScope;

import dagger.Component;

@BroadcastReceiverScope
@Component(dependencies = OpenSecretSantaApplicationComponent.class)
public interface SmsManagerSendReceiverComponent {

    void inject(SmsManagerSendReceiver smsManagerSendReceiver);
}

