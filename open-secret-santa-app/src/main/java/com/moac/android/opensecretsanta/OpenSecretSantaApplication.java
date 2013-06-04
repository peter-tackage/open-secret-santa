package com.moac.android.opensecretsanta;

import android.accounts.*;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import com.moac.android.opensecretsanta.activity.Constants;
import com.moac.android.opensecretsanta.database.DatabaseHelper;
import com.moac.android.opensecretsanta.database.DatabaseManager;

import java.io.IOException;

public class OpenSecretSantaApplication extends Application {

    private static final String TAG = "OpenSecretSantaApp";

    private static DatabaseManager mDatabaseManager = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabaseManager = initDatabase();
    }

    public static DatabaseManager getDatabase() {
        return mDatabaseManager;
    }

    private DatabaseManager initDatabase() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        return new DatabaseManager(databaseHelper);
    }

    public Account getAvailableGmailAccount() {
        Account result = null;
        // Use the one in the preferences, otherwise just pick the first one.
        String emailPrefKey =  getString(R.string.gmail_account_preference);
        String emailAddress = PreferenceManager.getDefaultSharedPreferences(this).getString(emailPrefKey, null);

            AccountManagerFuture<Account[]> accountsFuture =
              AccountManager.get(this).getAccountsByTypeAndFeatures(Constants.ACCOUNT_TYPE_GOOGLE, Constants.FEATURES_MAIL, null, null);
            try {
                Account[] accounts = accountsFuture.getResult();
                if(accounts != null && accounts.length > 0) {
                   if (emailAddress == null) {
                       // Set into preferences for next time.
                       PreferenceManager.getDefaultSharedPreferences(this).getString(emailPrefKey, accounts[0].name);
                       return accounts[0];
                   } else {
                       // Find matching account
                       for (int i=0; i < accounts.length; i++) {
                           Account acc = accounts[i];
                           if (acc.name.equals(emailAddress)){
                               result = acc;
                               break;
                           }
                       }
                   }
                }
            } catch(Exception e) {
               Log.e(TAG, "getAvailableGmailAccount() - Error when fetching account", e);
            }
          return result;
    }

}
