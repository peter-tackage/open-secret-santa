package com.moac.android.opensecretsanta;

import android.accounts.*;
import android.app.Application;
import android.preference.PreferenceManager;
import android.util.Log;
import com.moac.android.opensecretsanta.database.DatabaseHelper;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.mail.GmailOAuth2Sender;

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
        Log.i(TAG, "getAvailableGmailAccount() - start");
        Account result = null;
        // Use the one in the preferences, otherwise just pick the first one.
        String emailPrefKey =  getString(R.string.gmail_account_preference);
        String emailAddress = PreferenceManager.getDefaultSharedPreferences(this).getString(emailPrefKey, null);

        Log.v(TAG, "getAvailableGmailAccount() - current Gmail Account preference: " + emailAddress);

        AccountManagerFuture<Account[]> accountsFuture =
              AccountManager.get(this).getAccountsByTypeAndFeatures(GmailOAuth2Sender.ACCOUNT_TYPE_GOOGLE, GmailOAuth2Sender.FEATURES_MAIL, null, null);
            try {
                Account[] accounts = accountsFuture.getResult();
                if(accounts != null && accounts.length > 0) {
                    Log.v(TAG, "getAvailableGmailAccount() - found some Gmail Accounts, size: " + accounts.length);
                    if (emailAddress == null) {
                        Log.v(TAG, "getAvailableGmailAccount() - no preference, so use first Gmail account.");
                        //String token = AccountManager.get(this).peekAuthToken();
                        // Set into preferences for next time.
                       PreferenceManager.getDefaultSharedPreferences(this).edit().putString(emailPrefKey, accounts[0].name).commit();
                       return accounts[0];
                   } else {
                        Log.v(TAG, "getAvailableGmailAccount() - found Gmail preference: " + emailAddress);
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
        Log.v(TAG, "getAvailableGmailAccount() - returning Gmail Account: " + result);

        return result;
    }

}
