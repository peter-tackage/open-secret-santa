package com.moac.android.opensecretsanta.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.support.ConnectionSource;

public class TestDatabaseHelper extends DatabaseHelper {

    ConnectionSource mConnectionSource;

    protected TestDatabaseHelper(Context context, String databaseName, Class[] persistableObjects) {
        super(context, databaseName);
        PERSISTABLE_OBJECTS = persistableObjects;
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource cs) {
        mConnectionSource = cs;
        super.onCreate(db, cs);
    }
}
