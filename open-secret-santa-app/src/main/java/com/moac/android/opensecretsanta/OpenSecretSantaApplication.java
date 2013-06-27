package com.moac.android.opensecretsanta;

import android.accounts.*;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.moac.android.opensecretsanta.activity.ContactModes;
import com.moac.android.opensecretsanta.database.DatabaseHelper;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.mail.GmailOAuth2Sender;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.Restriction;
import com.moac.android.opensecretsanta.util.DrawEngineFactory;
import com.moac.android.opensecretsanta.util.InvalidDrawEngineException;
import com.moac.android.opensecretsanta.util.Utils;
import com.moac.drawengine.DrawEngine;

public class OpenSecretSantaApplication extends Application {

    private static final String TAG = "OpenSecretSantaApp";

    private static DatabaseManager sDatabaseManager = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sDatabaseManager = initDatabase();
        Utils.doOnce(getApplicationContext(), "initTestData", new Runnable() {
            @Override
            public void run() {
              loadTestData();
            }
        });
    }

    public static DatabaseManager getDatabase() {
        return sDatabaseManager;
    }

    private DatabaseManager initDatabase() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        return new DatabaseManager(databaseHelper);
    }

    // Returns an instance of the currently prefered DrawEngine
    public static DrawEngine getCurrentDrawEngineInstance(Context _context) throws InvalidDrawEngineException {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        String defaultName = _context.getString(R.string.defaultDrawEngine);
        String classname = prefs.getString("engine_preference",
          defaultName);

        Log.i(TAG, "getCurrentDrawEngineInstance() - setting draw engine to: " + classname);

        try {
            return DrawEngineFactory.createDrawEngine(classname);
        } catch(InvalidDrawEngineException ideexp) {
            // Error: If we weren't attempting to load the default name, then try that instead
            if(!classname.equals(defaultName)) {
                Log.w(TAG, "Failed to initialise draw engine class: " + classname);
                try {
                    // Try to set the default then.
                    DrawEngine engine = DrawEngineFactory.createDrawEngine(defaultName);
                    // Success - update preference to use the default.
                    prefs.edit().putString("engine_preference", defaultName).commit();
                    return engine;
                } catch(InvalidDrawEngineException ideexp2) {
                    Log.e(TAG, "Unable to initialise default draw engine class: " + classname, ideexp2);
                    throw ideexp2;
                }
            }
            throw ideexp;
        }
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
        createTestDraw(0);
        createTestDraw(1);
        createTestDraw(2);
        createTestDraw(3);
        createTestDraw(4);
     }

    private void createTestDraw(int _instance ) {
        // Add a Group
        Group group1 = new Group();
        group1.setName("Test Group - 1 " + _instance);
        sDatabaseManager.create(group1);

        // Add some Members
        Member m1 = new Member();
        m1.setName("John Goodman"+_instance);
        m1.setContactMode(ContactModes.NAME_ONLY_CONTACT_MODE);

        Member m2 = new Member();
        m2.setName("Mary Arthur"+_instance);
        m2.setContactMode(ContactModes.EMAIL_CONTACT_MODE);
        m2.setContactAddress("test@tester.com");

        Member m3 = new Member();
        m3.setName("Some Person"+_instance);
        m3.setContactMode(ContactModes.SMS_CONTACT_MODE);
        m3.setContactAddress("+49232267513213");

        m1.setGroup(group1);
        m2.setGroup(group1);
        m3.setGroup(group1);
        sDatabaseManager.create(m1);
        sDatabaseManager.create(m2);
        sDatabaseManager.create(m3);

        Restriction r1 = new Restriction();
        r1.setMember(m1);
        r1.setOtherMember(m2);
        sDatabaseManager.create(r1);
    }



}
