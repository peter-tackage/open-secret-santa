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
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.SqliteAndroidDatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import com.moac.android.opensecretsanta.types.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "opensecretsanta.db";
    private static final int DATABASE_VERSION = 2;

    private static final Class[] PERSISTABLE_OBJECTS =
      { DrawResult.class, DrawResultEntry.class, Group.class, Member.class, Restriction.class };

    private final Map<Class<? extends PersistableObject>, Dao<? extends PersistableObject, Long>> daos =
      new HashMap<Class<? extends PersistableObject>, Dao<? extends PersistableObject, Long>>();

    private final Map<Class<? extends PersistableObject>, DatabaseTableConfig<? extends PersistableObject>> tableConfigs =
      new HashMap<Class<? extends PersistableObject>, DatabaseTableConfig<? extends PersistableObject>>();

    public DatabaseHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // For testing use only
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
            db.beginTransaction();

            boolean success = true;
            for(int i = oldVersion; i < newVersion; ++i) {
                int nextVersion = i + 1;
                switch(nextVersion) {
                    case 2:
                        success = upgradeToVersion2(db);
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
    public <T extends PersistableObject> Dao<T, Long> getDaoEx(Class<T> modelClass) {
        Dao<T, Long> result = null;
        if(daos.containsKey(modelClass)) {
            result = (Dao<T, Long>) daos.get(modelClass);
        } else {
            try {
                result = getDao(modelClass);
            } catch(java.sql.SQLException e) {
                throw new SQLException(e.getMessage());
            }
            daos.put(modelClass, result);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T extends PersistableObject> DatabaseTableConfig<T> getTableConfig(Class<T> modelClass) {
        DatabaseTableConfig<T> result = null;
        if(tableConfigs.containsKey(modelClass)) {
            result = (DatabaseTableConfig<T>) tableConfigs.get(modelClass);
        } else {
            try {
                result = DatabaseTableConfig.fromClass(getConnectionSource(), modelClass);
            } catch(java.sql.SQLException e) {
                throw new SQLException(e.getMessage());
            }
            tableConfigs.put(modelClass, result);
        }
        return result;
    }

    public <T extends PersistableObject> String getTableName(Class<T> modelClass) {
        DatabaseTableConfig<? extends PersistableObject> cfg = getTableConfig(modelClass);
        return cfg.getTableName();
    }

    public <T extends PersistableObject> String[] getColumnNames(Class<T> modelClass, boolean foreignOnly) {
        List<String> columnNames = new ArrayList<String>();
        try {
            DatabaseTableConfig<? extends PersistableObject> cfg = getTableConfig(modelClass);
            SqliteAndroidDatabaseType dbType = new SqliteAndroidDatabaseType();
            for(FieldType fieldType : cfg.getFieldTypes(dbType)) {
                if(!foreignOnly || fieldType.isForeign()) {
                    columnNames.add(fieldType.getColumnName());
                }
            }
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
        return columnNames.toArray(new String[]{ });
    }

    private void createTables(SQLiteDatabase db, ConnectionSource cs) {
        for(Class<? extends PersistableObject> modelClass : PERSISTABLE_OBJECTS) {
            createTable(modelClass, cs);
        }
    }

    private void createTable(Class<? extends PersistableObject> modelClass, ConnectionSource cs) {
        try {
            TableUtils.createTable(cs, modelClass);
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public <T extends PersistableObject> List<T> queryAll(Class<T> modelClass) {
        List<T> entity;
        try {
            entity = getDaoEx(modelClass).queryForAll();
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
        return entity;
    }

    public <T extends PersistableObject> T queryById(long id, Class<T> modelClass) {
        T entity;
        try {
            entity = getDaoEx(modelClass).queryForId(id);
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
        return entity;
    }

    public <T extends PersistableObject> long create(PersistableObject entity, Class<T> modelClass) {
        long id = PersistableObject.UNSET_ID;
        try {
            if(getDaoEx(modelClass).create(modelClass.cast(entity)) == 1) {
                id = entity.getId();
            }
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
        return id;
    }

    public <T extends PersistableObject> void update(PersistableObject entity, Class<T> modelClass) {
        try {
            int count = getDaoEx(modelClass).update(modelClass.cast(entity));
            assert (count == 1);
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public <T extends PersistableObject> void deleteById(long id, Class<T> modelClass) {
        try {
            int count = getDaoEx(modelClass).deleteById(id);
            assert (count == 1);
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    /*
     * Database Schema Upgrade Methods
     */
    private boolean upgradeToVersion2(SQLiteDatabase db) {

        Log.i(TAG, "upgradeToVersion2 - start.");

        // Something like - update members set contact_detail = null  where contact_detail = '';

        ContentValues values = new ContentValues();
        values.putNull(Member.Columns.CONTACT_DETAIL_COLUMN);

        int updatedRows = db.update(Member.TABLE_NAME, values, Member.Columns.CONTACT_DETAIL_COLUMN + " = ''", null);
        Log.i(TAG, "upgradeToVersion2 - updatedRows: " + updatedRows);
        return true;
    }
}
