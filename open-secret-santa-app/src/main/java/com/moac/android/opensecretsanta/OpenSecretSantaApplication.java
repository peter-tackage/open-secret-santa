package com.moac.android.opensecretsanta;

import android.accounts.*;
import android.app.Application;
import android.preference.PreferenceManager;
import android.util.Log;
import com.moac.android.opensecretsanta.activity.ContactModes;
import com.moac.android.opensecretsanta.database.DatabaseHelper;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.mail.GmailOAuth2Sender;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.util.Utils;

public class OpenSecretSantaApplication extends Application {

    private static final String TAG = "OpenSecretSantaApp";

    private static DatabaseManager mDatabaseManager = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabaseManager = initDatabase();
        Utils.doOnce(getApplicationContext(), "initTestData", new Runnable() {
            @Override
            public void run() {
              loadTestData();
            }
        });
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


    private void loadTestData() {

        // Add a Group
        Group group1 = new Group();
        group1.setName("Test Group");
        mDatabaseManager.create(group1);

        // Add some Members
        Member m1 = new Member();
        m1.setName("John Goodman");
        m1.setContactMode(ContactModes.NAME_ONLY_CONTACT_MODE);

        Member m2 = new Member();
        m2.setName("Mary Arthur");
        m2.setContactMode(ContactModes.EMAIL_CONTACT_MODE);
        m2.setContactAddress("test@tester.com");

        Member m3 = new Member();
        m3.setName("Some Person");
        m3.setContactMode(ContactModes.SMS_CONTACT_MODE);
        m3.setContactAddress("+49232267513213");

        m1.setGroup(group1);
        m2.setGroup(group1);
        m3.setGroup(group1);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);
        mDatabaseManager.create(m3);
    }



}
