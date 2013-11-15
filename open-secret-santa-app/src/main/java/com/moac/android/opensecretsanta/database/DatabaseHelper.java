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
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import com.moac.android.opensecretsanta.model.*;
import com.moac.android.opensecretsanta.model.migration.OldDrawResult;
import com.moac.android.opensecretsanta.model.migration.OldDrawResultEntry;
import com.moac.android.opensecretsanta.model.migration.OldGroup;
import com.moac.android.opensecretsanta.model.migration.OldMember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "opensecretsanta.db";
    private static final int DATABASE_VERSION = 3;

    private static final Class[] PERSISTABLE_OBJECTS =
      { Group.class, Member.class, Restriction.class, Assignment.class };

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
                    case 3:
                       // success = upgradeToVersion3(db);
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

    private boolean upgradeToVersion3(SQLiteDatabase db) {

        // TODO set default values
        // TODO Rename columns or not

        // Member table now has an extra contact_id. why has contact_detail changed to contact_address
        db.execSQL("ALTER TABLE '" + Member.TABLE_NAME + "' ADD COLUMN " + Member.Columns.CONTACT_ID + " INTEGER;");

        // Restriction table remains unchanged

        // Group table now has extra message, created_at, draw_date and is dropping isReady
        db.execSQL("ALTER TABLE '" + Group.TABLE_NAME + "' ADD COLUMN " + Group.Columns.MESSAGE_COLUMN + " STRING;");
        db.execSQL("ALTER TABLE '" + Group.TABLE_NAME + "' ADD COLUMN " + Group.Columns.CREATED_AT_COLUMN + " INTEGER;");
        db.execSQL("ALTER TABLE '" + Group.TABLE_NAME + "' ADD COLUMN " + Group.Columns.DRAW_DATE_COLUMN + " INTEGER;");

        db.execSQL("ALTER TABLE '" + Group.TABLE_NAME + "' DROP COLUMN " + OldGroup.Columns.IS_READY);

        // The trickiest one is building the Assignment table
        // i) for each group, get all the Draw Results
        //    delete that group entry
        //    and with each Draw Result, add a new entry as a new Group (name is group + draw date)
        //    so each old Draw Results will now be a new Group (new schema rules)
        //    - migrate the message from Draw Result to the Group
        //    - migrate the draw date from Draw Result to the Group
        //    - set Group created date = draw date

        List<OldGroup> oldGroups = getAllOldGroups(db);
        for (OldGroup oldGroup : oldGroups) {
            List<OldDrawResult> oldDrawResults = getAllDrawResultsForGroup(db, oldGroup.getId());
            for (OldDrawResult oldDrawResult : oldDrawResults) {
                updateMigratedGroupInfo(db, oldDrawResult, oldGroup.getName());
                // for each Draw Result Entry, build the entry for the new Assignment
                //    Assignment columns are
                //    - giver member id and receiver member id
                //     (you need to get the member id from the given member name in the Member table)
                //    - send status
                //     (work it out from the Draw Result Entry sent and/or viewed date, if not set to assigned)
                //
                List<OldDrawResultEntry> oldDrawResultEntries = getAllOldDrawResultEntries(db);
                for (OldDrawResultEntry oldDrawResultEntry : oldDrawResultEntries) {
                    insertMigratedAssignmentEntryInfo(db, oldDrawResultEntry);
                }
            }

            // remove old group
            removeOldGroup(db, oldGroup.getId());
        }

        // let's not drop any tables (yet) just in case we need to rollback....

        return true;
    }

    private void removeOldGroup(SQLiteDatabase db, long oldGroupId) {
        db.delete(Group.TABLE_NAME, Group.Columns._ID + " = ?",
                new String[] { String.valueOf(oldGroupId) });
    }

    private List<OldGroup> getAllOldGroups(SQLiteDatabase db) {

        List<OldGroup> oldGroups = new ArrayList<OldGroup>();

        Cursor cursor = db.query("Group",
                OldGroup.Columns.ALL, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            OldGroup oldGroup = cursorToOldGroup(cursor);
            oldGroups.add(oldGroup);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        return oldGroups;
    }

    private OldGroup cursorToOldGroup(Cursor cursor) {
        OldGroup oldGroup = new OldGroup();
        oldGroup.setName(cursor.getString(cursor.getColumnIndex(OldGroup.Columns.NAME_COLUMN)));
        return oldGroup;
    }

    private List<OldDrawResult> getAllDrawResultsForGroup(SQLiteDatabase db, long oldGroupId) {
        // get the most recent draw result based on Draw Date
        String selectQuery = "SELECT  * FROM " + "Draw Results" + " WHERE "
                + OldDrawResult.Columns.GROUP_ID_COLUMN + " = " + oldGroupId;

        Log.i(TAG, selectQuery);
        List<OldDrawResult> oldDrawResults = new ArrayList<OldDrawResult>();

        Cursor c = db.rawQuery(selectQuery, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            OldDrawResult oldDrawResult = cursorToOldDrawResult(c);
            oldDrawResults.add(oldDrawResult);
            c.moveToNext();
        }

        return oldDrawResults;
    }

    private OldDrawResult cursorToOldDrawResult(Cursor c) {
        OldDrawResult oldDrawResult = new OldDrawResult();
        oldDrawResult.setDrawDate(c.getLong(c.getColumnIndex(OldDrawResult.Columns.DRAW_DATE_COLUMN)));
        oldDrawResult.setSendDate(c.getLong(c.getColumnIndex(OldDrawResult.Columns.SEND_DATE_COLUMN)));
        oldDrawResult.setMessage(c.getString(c.getColumnIndex(OldDrawResult.Columns.MESSAGE_COLUMN)));
        return oldDrawResult;
    }

    private void updateMigratedGroupInfo(SQLiteDatabase db, OldDrawResult oldDrawResult, String oldGroupName) {
        // find the group row and update the details to match the new Group
        ContentValues values = new ContentValues();
        values.put(Group.Columns.NAME_COLUMN, oldGroupName + " " + oldDrawResult.getDrawDate());
        values.put(Group.Columns.MESSAGE_COLUMN, oldDrawResult.getMessage());
        values.put(Group.Columns.DRAW_DATE_COLUMN, oldDrawResult.getDrawDate());

        // this is  new field and we don't have old data for that, so we will set created date = draw date
        values.put(Group.Columns.CREATED_AT_COLUMN, oldDrawResult.getDrawDate());

        // insert row
        db.insert(Group.TABLE_NAME, null, values);
    }


    private List<OldDrawResultEntry> getAllOldDrawResultEntries(SQLiteDatabase db) {

        List<OldDrawResultEntry> oldDrawResultEntries = new ArrayList<OldDrawResultEntry>();

        Cursor cursor = db.query("Draw Result Entry",
                OldDrawResultEntry.Columns.ALL, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            OldDrawResultEntry oldDrawResultEntry= cursorToOldDrawResultEntry(cursor);
            oldDrawResultEntries.add(oldDrawResultEntry);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        return oldDrawResultEntries;
    }

    private OldDrawResultEntry cursorToOldDrawResultEntry(Cursor cursor) {
        OldDrawResultEntry oldDrawResultEntry = new OldDrawResultEntry();
        oldDrawResultEntry.setGiverName(cursor.getString(cursor.getColumnIndex(OldDrawResultEntry.Columns.MEMBER_NAME_COLUMN)));
        oldDrawResultEntry.setReceiverName(cursor.getString(cursor.getColumnIndex(OldDrawResultEntry.Columns.OTHER_MEMBER_NAME_COLUMN)));
        oldDrawResultEntry.setContactDetail(cursor.getString(cursor.getColumnIndex(OldDrawResultEntry.Columns.CONTACT_DETAIL_COLUMN)));
        oldDrawResultEntry.setViewedDate(cursor.getLong(cursor.getColumnIndex(OldDrawResultEntry.Columns.VIEWED_DATE_COLUMN)));
        oldDrawResultEntry.setSentDate(cursor.getLong(cursor.getColumnIndex(OldDrawResultEntry.Columns.SENT_DATE_COLUMN)));
        return oldDrawResultEntry;
    }

    private void  insertMigratedAssignmentEntryInfo(SQLiteDatabase db, OldDrawResultEntry oldDrawResultEntry) {
        int giverMemberId = getMemberIdFromMemberName(db, oldDrawResultEntry.getGiverName());
        int receiverMemberId = getMemberIdFromMemberName(db, oldDrawResultEntry.getReceiverName());

        ContentValues values = new ContentValues();
        values.put(Assignment.Columns.GIVER_MEMBER_ID_COLUMN, giverMemberId);
        values.put(Assignment.Columns.RECEIVER_MEMBER_ID_COLUMN, receiverMemberId);

        Assignment.Status sendStatus = Assignment.Status.Assigned; // default
        if (oldDrawResultEntry.getSentDate() > 0) {
            sendStatus = Assignment.Status.Sent;
        } else if (oldDrawResultEntry.getViewedDate() > 0) {
            sendStatus = Assignment.Status.Revealed;
        }
        values.put(Assignment.Columns.SEND_STATUS_COLUMN, sendStatus.getText());

        // insert row
        db.insert(Assignment.TABLE_NAME, null, values);
    }

    private int getMemberIdFromMemberName(SQLiteDatabase db, String memberName) {
        // get the most recent draw result based on Draw Date
        String selectQuery = "SELECT " + OldMember.Columns._ID + " FROM " + OldMember.TABLE_NAME + " WHERE "
                + OldMember.Columns.NAME_COLUMN + " = " + memberName;

        Log.i(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);
        c.moveToFirst();
        return c.getInt(c.getColumnIndex(OldMember.Columns._ID));
    }
}
