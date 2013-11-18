package com.moac.android.opensecretsanta;

import android.app.Application;
import com.moac.android.opensecretsanta.database.DatabaseHelper;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.ContactMethod;
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

    private void createTestDraw(int _instance) {
        // Add a Group
        Group group1 = new Group();
        group1.setName("Party " + _instance);
        mDatabaseManager.create(group1);

        // Add some Members
        Member m1 = new Member();
        m1.setName("Jasmine");
        m1.setContactMethod(ContactMethod.REVEAL_ONLY);

        Member m2 = new Member();

        m2.setName("James");
        m2.setContactMethod(ContactMethod.EMAIL);
        m2.setContactDetails("me@somedomaiincalledjames.com");

        Member m3 = new Member();
        m3.setName("Mandy");
        m3.setContactMethod(ContactMethod.SMS);
        m3.setContactDetails("+1083218723");

        Member m4 = new Member();
        m4.setName("Patrick");
        m4.setContactMethod(ContactMethod.SMS);
        m4.setContactDetails("+9827343983");

        m1.setGroup(group1);
        m2.setGroup(group1);
        m3.setGroup(group1);
        m4.setGroup(group1);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);
        mDatabaseManager.create(m3);
        mDatabaseManager.create(m4);

        Restriction r1 = new Restriction();
        r1.setMember(m1);
        r1.setOtherMember(m2);
        mDatabaseManager.create(r1);
    }
}
