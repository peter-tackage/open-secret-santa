package com.moac.android.opensecretsanta.database;

/*
*
* Derived from the Feeder app.
*
* Copyright (C) 2012 Stoyan Rachev (stoyanr@gmail.com)
*
* This program is free software; you can redistribute it and/or modify it
* under the terms of the GNU General Public License as published by the
* Free Software Foundation; either version 2, or (at your option) any
* later version.
*
* This program is distributed in the hope that it will be useful, but
* WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* General Public License for more details.
*/

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.google.common.annotations.VisibleForTesting;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import com.moac.android.opensecretsanta.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "opensecretsanta.db";
    private static final int DATABASE_VERSION = 3;

    protected Class[] PERSISTABLE_OBJECTS;

    private final Map<Class<? extends PersistableObject>, Dao<? extends PersistableObject, Long>> daos =
      new HashMap<Class<? extends PersistableObject>, Dao<? extends PersistableObject, Long>>();

    private final Map<Class<? extends PersistableObject>, DatabaseTableConfig<? extends PersistableObject>> tableConfigs =
      new HashMap<Class<? extends PersistableObject>, DatabaseTableConfig<? extends PersistableObject>>();

    public DatabaseHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        PERSISTABLE_OBJECTS = new Class[] {Group.class, Member.class, Restriction.class, Assignment.class};
    }

    @VisibleForTesting
    protected DatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource cs) {
        createTables(db, cs);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {
        Log.v(TAG, "onUpgrade() - start);");

        if(newVersion > oldVersion) {

            // upgrade part one that would include change in the schema etc
            db.beginTransaction();

            boolean success = true;
            for(int i = oldVersion; i < newVersion; ++i) {
                int nextVersion = i + 1;
                switch(nextVersion) {
                    case 2:
                        success = upgradeToVersion2(db);
                        break;
                    case 3:
                        success = upgradeSchemaToVersion3(connectionSource);
                        break;
                }
                if(!success) {
                    break;
                }
            }
            if(success) {
                db.setTransactionSuccessful();
            }
            db.endTransaction();

            // upgrade part two that would include migrating the data but only if part one passed

            Log.d(TAG, "Did upgrade schema go ok? Are we going to the next migrating data step? " + success);
            if (success) {
                // should this fail, we will roll back all the changes and not migrate anything
                // as we still have a workable DB with the new schema
                // we handle these db transaction at a deeper level
                for(int i = oldVersion; i < newVersion; ++i) {
                    int nextVersion = i + 1;
                    switch(nextVersion) {
                        case 3:
                            migrateDataToVersion3(db, connectionSource);
                            break;
                    }
                 }
            }

        } else {
            onCreate(db);
        }

        Log.v(TAG, "onUpgrade() - end);");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if(!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void close() {
        super.close();
        daos.clear();
        tableConfigs.clear();
    }

    @SuppressWarnings("unchecked")
    public <T extends PersistableObject> Dao<T, Long> getDaoEx(Class<T> objClass) {
        Dao<T, Long> result;
        if(daos.containsKey(objClass)) {
            result = (Dao<T, Long>) daos.get(objClass);
        } else {
            try {
                result = getDao(objClass);
            } catch(java.sql.SQLException e) {
                throw new SQLException(e.getMessage());
            }
            daos.put(objClass, result);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void createTables(SQLiteDatabase db, ConnectionSource cs) {
        for(Class<? extends PersistableObject> objClass : PERSISTABLE_OBJECTS) {
            createTable(objClass, cs);
        }
    }

    private void createTable(Class<? extends PersistableObject> objClass, ConnectionSource cs) {
        try {
            TableUtils.createTable(cs, objClass);
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public <T extends PersistableObject> List<T> queryAll(Class<T> objClass) {
        List<T> entity;
        try {
            entity = getDaoEx(objClass).queryForAll();
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
        return entity;
    }

    public <T extends PersistableObject> T queryById(long id, Class<T> objClass) {
        T entity;
        try {
            entity = getDaoEx(objClass).queryForId(id);
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
        return entity;
    }

    public <T extends PersistableObject> long create(PersistableObject entity, Class<T> objClass) {
        long id = PersistableObject.UNSET_ID;
        try {
            if(getDaoEx(objClass).create(objClass.cast(entity)) == 1) {
                id = entity.getId();
            }
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
        return id;
    }

    public <T extends PersistableObject> void update(PersistableObject entity, Class<T> objClass) {
        try {
            int count = getDaoEx(objClass).update(objClass.cast(entity));
            assert (count == 1);
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public <T extends PersistableObject> void deleteById(long id, Class<T> objClass) {
        try {
            int count = getDaoEx(objClass).deleteById(id);
            assert (count == 1);
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    /*
     * Database Schema Upgrade Methods
     */

    protected boolean upgradeToVersion2(SQLiteDatabase db) {
        Log.i(TAG, "upgradeToVersion2 - start.");

        // Something like - update members set contact_detail = null  where contact_detail = '';
        final String CONTACT_DETAIL_COLUMN="CONTACT_DETAIL"; // Hard code old column name.
        ContentValues values = new ContentValues();
        values.putNull(CONTACT_DETAIL_COLUMN);

        int updatedRows = db.update(Member.TABLE_NAME, values, CONTACT_DETAIL_COLUMN + " = ''", null);
        Log.i(TAG, "upgradeToVersion2 - updatedRows: " + updatedRows);
        return true;
    }

    protected boolean upgradeSchemaToVersion3(ConnectionSource cs) {
        try {
            Log.d(TAG, "upgradeSchemaToVersion3");
            DatabaseUpgrader databaseUpgrader = new DatabaseUpgrader(this);
            databaseUpgrader.upgradeDatabaseSchemaToVersion3(cs);
            Log.d(TAG, "upgradeDatabaseSchemaToVersion3 returned with no exception");
            // let's not drop any tables
            return true;
        } catch (Exception e) {
            Log.e(TAG, "upgradeDatabaseSchemaToVersion3 threw exception:" + e.getMessage());
            return false;
        }
    }

    protected void migrateDataToVersion3(SQLiteDatabase db, ConnectionSource cs) {
        Log.d(TAG, "migrateDataToVersion3");
        DatabaseUpgrader databaseUpgrader = new DatabaseUpgrader(this);
        databaseUpgrader.migrateDataToVersion3(db, cs);
    }
}
