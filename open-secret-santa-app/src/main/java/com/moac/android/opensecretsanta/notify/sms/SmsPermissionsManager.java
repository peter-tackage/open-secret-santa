package com.moac.android.opensecretsanta.notify.sms;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Telephony;

import com.moac.android.opensecretsanta.BuildConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SmsPermissionsManager {

    private final SharedPreferences mPreferences;
    private static final String DEFAULT_SMS_APP_KEY = "default_sms_app_package";

    @Inject
    public SmsPermissionsManager(SharedPreferences preferences) {
        mPreferences = preferences;
    }

    public void requestDefaultSmsPermission(Context context, Fragment fragment, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context.getApplicationContext());
            saveDefaultSmsApp(defaultSmsApp);
            if (!defaultSmsApp.equals(BuildConfig.PACKAGE_NAME)) {
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, BuildConfig.PACKAGE_NAME);
                fragment.startActivityForResult(intent, requestCode);
            }
        }
    }

    public void requestRelinquishDefaultSmsPermission(Fragment fragment, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String defaultSmsApp = getLastKnownDefaultSmsApp();
            // If the recorded previous default isn't OSS; ask to change it
            if (!defaultSmsApp.equals(BuildConfig.PACKAGE_NAME)) {
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, defaultSmsApp);
                fragment.startActivityForResult(intent, requestCode);
            }
        }
    }

    public void clearSavedDefaultSmsApp() {
        mPreferences.edit().remove(DEFAULT_SMS_APP_KEY).apply();
    }

    protected void saveDefaultSmsApp(String defaultSmsApp) {
        mPreferences.edit().putString(DEFAULT_SMS_APP_KEY, defaultSmsApp).apply();
    }

    protected String getLastKnownDefaultSmsApp() {
        return mPreferences.getString(DEFAULT_SMS_APP_KEY, BuildConfig.PACKAGE_NAME);
    }
}
