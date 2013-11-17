package com.moac.android.opensecretsanta.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.support.ConnectionSource;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.version2.DrawResultEntryVersion2;
import com.moac.android.opensecretsanta.model.version2.DrawResultVersion2;
import com.moac.android.opensecretsanta.model.version2.GroupVersion2;
import com.moac.android.opensecretsanta.model.version2.MemberVersion2;

/**
 * Created with IntelliJ IDEA.
 * User: amelysh
 * Date: 16.11.13
 * Time: 00:59
 * To change this template use File | Settings | File Templates.
 */
public class TestDatabaseHelper extends DatabaseHelper {

    protected TestDatabaseHelper(Context context, String databaseName, Class[] persistableObjects) {
        super(context, databaseName);
        PERSISTABLE_OBJECTS = persistableObjects;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        onCreate(db, connectionSource);
    }

    protected void dropAllTables() {
        getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + GroupVersion2.TABLE_NAME);
        getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + MemberVersion2.TABLE_NAME);
        getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + DrawResultVersion2.TABLE_NAME);
        getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + DrawResultEntryVersion2.TABLE_NAME);
        getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + Assignment.TABLE_NAME);
        getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + Group.TABLE_NAME);
    }
}
