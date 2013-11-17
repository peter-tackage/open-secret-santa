package com.moac.android.opensecretsanta;

import android.accounts.*;
import android.app.Application;
import android.preference.PreferenceManager;
import android.util.Log;
import com.moac.android.opensecretsanta.model.ContactMode;
import com.moac.android.opensecretsanta.database.DatabaseHelper;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.notify.mail.GmailOAuth2Sender;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.Restriction;
import com.moac.android.opensecretsanta.util.Utils;

public class OpenSecretSantaApplication extends Application {

    private static final String TAG = "OpenSecretSantaApp";

    private static OpenSecretSantaApplication sInstance;
    private DatabaseManager mDatabaseManager;

    public OpenSecretSantaApplication() {
        super();
        if(sInstance == null)
            sInstance = this; // init self singleton.
    }
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

    public static OpenSecretSantaApplication getInstance() {
        return sInstance;
    }

    public DatabaseManager getDatabase() {
        return mDatabaseManager;
    }

    private DatabaseManager initDatabase() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        return new DatabaseManager(databaseHelper);
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
        mDatabaseManager.create(group1);

        // Add some Members
        Member m1 = new Member();
        m1.setName("John Goodman"+_instance);
        m1.setContactMode(ContactMode.REVEAL_ONLY);

        Member m2 = new Member();
        m2.setName("Mary Arthur"+_instance);
        m2.setContactMode(ContactMode.EMAIL);
        m2.setContactAddress("peter.tackage@gmail.com");

        Member m3 = new Member();
        m3.setName("Some Person"+_instance);
        m3.setContactMode(ContactMode.SMS);
        m3.setContactAddress("+4923229967513213");

        m1.setGroup(group1);
        m2.setGroup(group1);
        m3.setGroup(group1);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);
        mDatabaseManager.create(m3);

        Restriction r1 = new Restriction();
        r1.setMember(m1);
        r1.setOtherMember(m2);
        mDatabaseManager.create(r1);
    }



}
