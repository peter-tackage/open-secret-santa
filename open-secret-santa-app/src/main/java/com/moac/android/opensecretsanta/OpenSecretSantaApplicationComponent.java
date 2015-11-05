package com.moac.android.opensecretsanta;

import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.inject.base.component.BaseApplicationComponent;
import com.moac.android.opensecretsanta.inject.base.module.BaseApplicationModule;
import com.moac.android.opensecretsanta.instrumentation.Instrumentation;
import com.moac.android.opensecretsanta.module.AppModule;
import com.moac.android.opensecretsanta.notify.mail.EmailTransporter;
import com.moac.android.opensecretsanta.notify.sms.SmsPermissionsManager;
import com.moac.android.opensecretsanta.notify.sms.SmsTransporter;
import com.squareup.otto.Bus;

import android.accounts.AccountManager;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {BaseApplicationModule.class,
                      AppModule.class})
public interface OpenSecretSantaApplicationComponent extends BaseApplicationComponent {

    DatabaseManager getDatabaseManager();

    SharedPreferences getDefaultSharedPreferences();

    Instrumentation getInstrumentation();

    SmsPermissionsManager smsPermissionManager();

    Bus getBus();

    AccountManager getAccountManager();

    SmsTransporter getSmsTransporter();

    EmailTransporter getEmailTransporter();

    void inject(OpenSecretSantaApplication openSecretSantaApplication);

}
