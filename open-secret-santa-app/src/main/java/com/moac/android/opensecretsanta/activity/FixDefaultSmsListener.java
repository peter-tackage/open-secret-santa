package com.moac.android.opensecretsanta.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.moac.android.opensecretsanta.BuildConfig;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.notify.sms.SmsPermissionsManager;
import com.moac.android.opensecretsanta.util.NotifyUtils;

public class FixDefaultSmsListener implements View.OnClickListener {

    private static final String TAG = FixDefaultSmsListener.class.getSimpleName();

    private final Context mContext;
    private final SmsPermissionsManager mSmsPermissionsManager;

    public FixDefaultSmsListener(Context context, SmsPermissionsManager smsPermissionsManager) {
        mContext = context;
        mSmsPermissionsManager = smsPermissionsManager;
    }

    @Override
    public void onClick(View v) {
        // No action necessary when this app is not the default
        if (!NotifyUtils.isDefaultSmsApp(mContext)) return;

        // Is there a recorded default app?
        String defaultApp = mSmsPermissionsManager.getLastKnownDefaultSmsApp();

        if (!TextUtils.isEmpty(defaultApp) && !defaultApp.equals(BuildConfig.PACKAGE_NAME) && isAppInstalled(defaultApp)) {
            Log.d(TAG, "Previous default SMS app is still installed, attempt to revert");
            // There is a recorded default that isn't this app and it is still installed - attempt to set that as default
            // I guess this could fail if in between usage of this app, the original app changed to no longer be default SMS app capable
            // TODO Should we be clearing the default after this? We would have to handle onActivityResult
            mSmsPermissionsManager.requestRelinquishDefaultSmsPermission(mContext, defaultApp);

        } else {
            // Open Settings
            Log.d(TAG, "Attempting to launch settings");
            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.startActivity(intent);
                return;
            }
            // Couldn't help the user - show Toast to apologize
            Toast.makeText(mContext, mContext.getString(R.string.sms_default_setting_revert_failed), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = mContext.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}

