package com.moac.android.opensecretsanta.notify.sms;

import com.moac.android.opensecretsanta.OpenSecretSantaApplicationComponent;

import dagger.Component;

//@Component(dependencies = OpenSecretSantaApplicationComponent.class)
public interface SmsManagerSendReceiverComponent {

    void inject(SmsManagerSendReceiver smsManagerSendReceiver);
}

