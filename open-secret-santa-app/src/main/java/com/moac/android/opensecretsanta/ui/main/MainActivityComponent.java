package com.moac.android.opensecretsanta.ui.main;

import com.moac.android.opensecretsanta.OpenSecretSantaApplicationComponent;
import com.moac.android.opensecretsanta.inject.base.component.ActivityScope;
import com.moac.android.opensecretsanta.inject.base.component.BaseActivityComponent;
import com.moac.android.opensecretsanta.inject.base.module.BaseActivityModule;
import com.moac.android.opensecretsanta.notify.sms.SmsTransporter;

import dagger.Component;

@ActivityScope
@Component(dependencies = OpenSecretSantaApplicationComponent.class,
        modules = BaseActivityModule.class)
public interface MainActivityComponent extends BaseActivityComponent {

    void inject(MainActivity mainActivity);

    void inject(MemberListFragment memberListFragment);

    void inject(NotifyDialogFragment notifyDialogFragment);

    void inject(NotifyExecutorFragment notifyExecutorFragment);

}
