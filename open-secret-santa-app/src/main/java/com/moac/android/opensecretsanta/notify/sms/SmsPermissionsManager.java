package com.moac.android.opensecretsanta.notify.sms;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Telephony;

import com.moac.android.opensecretsanta.BuildConfig;
import com.moac.android.opensecretsanta.inject.ForApplication;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SmsPermissionsManager {

    private static final String DEFAULT_SMS_APP_KEY = "default_sms_app_package";

    private final Context mContext;
    private final SharedPreferences mPreferences;

    @Inject
    public SmsPermissionsManager(@ForApplication Context context, SharedPreferences preferences) {
        mContext = context;
        mPreferences = preferences;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void requestDefaultSmsPermission(Context context, Fragment fragment, int requestCode) {
        if (isPreKitKat()) return;

        String defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context.getApplicationContext());
        saveDefaultSmsApp(defaultSmsApp);
        if (!defaultSmsApp.equals(BuildConfig.PACKAGE_NAME)) {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, BuildConfig.PACKAGE_NAME);
            fragment.startActivityForResult(intent, requestCode);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void requestRelinquishDefaultSmsPermission(Fragment fragment, int requestCode) {
        if (isPreKitKat()) return;

        String defaultSmsApp = getLastKnownDefaultSmsApp();
        // If the recorded previous default isn't OSS and it's not the current default SMS app: ask to change it
        if (isRelinguishable(defaultSmsApp)) {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, defaultSmsApp);
            fragment.startActivityForResult(intent, requestCode);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void requestRelinquishDefaultSmsPermission(Context context, String toApp) {
        if (isPreKitKat()) return;

        // If the recorded previous default isn't OSS and it's not the current default SMS app: ask to change it
        if (isRelinguishable(toApp)) {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, toApp);
            context.startActivity(intent);
        }
    }

    public void clearSavedDefaultSmsApp() {
        mPreferences.edit().remove(DEFAULT_SMS_APP_KEY).apply();
    }

    protected void saveDefaultSmsApp(String defaultSmsApp) {
        mPreferences.edit().putString(DEFAULT_SMS_APP_KEY, defaultSmsApp).apply();
    }

    public String getLastKnownDefaultSmsApp() {
        return mPreferences.getString(DEFAULT_SMS_APP_KEY, BuildConfig.PACKAGE_NAME);
    }

    // Helpers

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean isRelinguishable(String toApp) {
        return !isPreKitKat()
                && !toApp.equals(BuildConfig.PACKAGE_NAME) // Don't relinquish to ourselves!
                && !Telephony.Sms.getDefaultSmsPackage(mContext).equals(toApp); // Don't relinquish if already default
    }

    // Pre Kitkat, we don't have the concept of Default SMS apps
    private static boolean isPreKitKat() {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT);
    }

}
