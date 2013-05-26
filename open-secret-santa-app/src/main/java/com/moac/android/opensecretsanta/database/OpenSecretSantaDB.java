package com.moac.android.opensecretsanta.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import com.moac.android.opensecretsanta.types.*;
import com.moac.android.opensecretsanta.types.Member.Columns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenSecretSantaDB {

    private final static String TAG = "OpenSecretSantaDB";

    private static final String DATABASE_NAME = "opensecretsanta.db";
    private static final int DATABASE_VERSION = 2;

    public static final String GROUPS_TABLE_NAME = "groups";
    public static final String MEMBERS_TABLE_NAME = "members";
    public static final String RESTRICTIONS_TABLE_NAME = "restrictions";
    public static final String DRAW_RESULTS_TABLE_NAME = "draw_results";
    public static final String DRAW_RESULT_ENTRIES_TABLE_NAME = "draw_result_entries";

    private static final String GROUPS_TABLE_CREATE =
      "CREATE TABLE " + " IF NOT EXISTS " + GROUPS_TABLE_NAME + " (" +
        Group.Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        Group.Columns.NAME_COLUMN + " TEXT NOT NULL UNIQUE, " +
        Group.Columns.IS_READY + " INTEGER " +
        " );";

    // A person in a given draw.
    private static final String MEMBER_TABLE_CREATE =
      "CREATE TABLE " + " IF NOT EXISTS " + MEMBERS_TABLE_NAME + " (" +
        Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        Member.Columns.LOOKUP_KEY + " TEXT, " +
        Member.Columns.NAME_COLUMN + " TEXT NOT NULL , " +
        Columns.CONTACT_MODE_COLUMN + " INTEGER NOT NULL, " +
        Member.Columns.CONTACT_DETAIL_COLUMN + " TEXT, " +
        Columns.GROUP_ID_COLUMN + " INTEGER, " +
        "FOREIGN KEY(" + Member.Columns.GROUP_ID_COLUMN + ") REFERENCES " + GROUPS_TABLE_NAME + " ON DELETE CASCADE " +
        " );";

    // A mapping of member to member.
    //
    private static final String RESTRICTIONS_TABLE_CREATE =
      "CREATE TABLE " + " IF NOT EXISTS " + RESTRICTIONS_TABLE_NAME + " (" +
        Restriction.Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        Restriction.Columns.MEMBER_ID_COLUMN + " INTEGER, " +
        Restriction.Columns.OTHER_MEMBER_ID_COLUMN + " INTEGER, " +
        "FOREIGN KEY(" + Restriction.Columns.MEMBER_ID_COLUMN + ") REFERENCES " + MEMBERS_TABLE_NAME
        + " ON DELETE CASCADE, " +
        "FOREIGN KEY(" + Restriction.Columns.OTHER_MEMBER_ID_COLUMN + ") REFERENCES " + MEMBERS_TABLE_NAME
        + " ON DELETE CASCADE " +
        " );";

    // A list of all the draw results
    private static final String DRAW_RESULTS_TABLE_CREATE =
      "CREATE TABLE " + " IF NOT EXISTS " + DRAW_RESULTS_TABLE_NAME + " (" +
        DrawResult.Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        DrawResult.Columns.DRAW_DATE_COLUMN + " INTEGER, " +
        DrawResult.Columns.SEND_DATE_COLUMN + " INTEGER, " +
        DrawResult.Columns.MESSAGE_COLUMN + " TEXT, " +
        DrawResult.Columns.GROUP_ID_COLUMN + " INTEGER, " +
        "FOREIGN KEY(" + DrawResult.Columns.GROUP_ID_COLUMN + ") REFERENCES " + GROUPS_TABLE_NAME
        + " ON DELETE CASCADE "
        + ");";

    private static final String DRAW_RESULTS_ENTRIES_TABLE_CREATE =
      "CREATE TABLE " + " IF NOT EXISTS " + DRAW_RESULT_ENTRIES_TABLE_NAME + " (" +
        DrawResultEntry.Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        DrawResultEntry.Columns.OTHER_MEMBER_NAME_COLUMN + " TEXT NOT NULL, " +
        DrawResultEntry.Columns.MEMBER_NAME_COLUMN + " TEXT NOT NULL, " +
        DrawResultEntry.Columns.DRAW_RESULT_ID_COLUMN + " INTEGER, " +
        DrawResultEntry.Columns.CONTACT_MODE_COLUMN + " INTEGER, " +
        DrawResultEntry.Columns.CONTACT_DETAIL_COLUMN + " TEXT, " +
        DrawResultEntry.Columns.VIEWED_DATE_COLUMN + " INTEGER, " +
        DrawResultEntry.Columns.SENT_DATE_COLUMN + " INTEGER, " +
        "FOREIGN KEY(" + DrawResultEntry.Columns.DRAW_RESULT_ID_COLUMN + ") REFERENCES " + DRAW_RESULTS_TABLE_NAME
        + " ON DELETE CASCADE "
        + ");";

    private SQLiteDatabase mDb;
    private DatabaseHelper mDbHelper;

    public OpenSecretSantaDB(Context _context) {
        Log.i(TAG, "##### OpenSecretSantaDB() start");

        mDbHelper = new DatabaseHelper(_context);
        try {
            mDb = mDbHelper.getWritableDatabase();
            // Enable foreign key constraints
            mDb.execSQL("PRAGMA foreign_keys=ON;");
        } catch(SQLiteException ex) {
            Log.e(TAG, "##### OpenSecretSantaDB() - " + ex);
            mDb = mDbHelper.getReadableDatabase();
        }
    }

    public void open() throws SQLException {
        try {
            mDb = mDbHelper.getWritableDatabase();
            // Enable foreign key constraints
            mDb.execSQL("PRAGMA foreign_keys=ON;");
        } catch(SQLiteException ex) {
            mDb = mDbHelper.getReadableDatabase();
        }
    }

    public void close() {
        mDb.close();
    }

    /**
     * @author peter
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final String TAG = "OpenSecretSantaDB.DatabaseHelper";

        /**
         * @param context
         */
        private DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.v(TAG, "onCreate() - start);");

            db.execSQL(GROUPS_TABLE_CREATE);
            db.execSQL(MEMBER_TABLE_CREATE);
            db.execSQL(RESTRICTIONS_TABLE_CREATE);
            db.execSQL(DRAW_RESULTS_TABLE_CREATE);
            db.execSQL(DRAW_RESULTS_ENTRIES_TABLE_CREATE);

            Log.v(TAG, "onCreate() - end);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

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

        private boolean upgradeToVersion2(SQLiteDatabase db) {

            Log.i(TAG, "upgradeToVersion2 - start.");

            // Something like - update members set contact_detail = null  where contact_detail = '';

            ContentValues values = new ContentValues();
            values.putNull(Columns.CONTACT_DETAIL_COLUMN);

            int updatedRows = db.update(MEMBERS_TABLE_NAME, values, Columns.CONTACT_DETAIL_COLUMN + " = ''", null);
            Log.i(TAG, "upgradeToVersion2 - updatedRows: " + updatedRows);
            return true;
        }
    }

	/*
	 * Groups
	 */

    public long insertGroup(Group _group) {
        Log.v(TAG, "insertGroup() - start);");
        long groupId;
        try {
            mDb.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(Group.Columns.NAME_COLUMN, _group.getName());
            values.put(Group.Columns.IS_READY, _group.isReady() ? 1 : 0);

            groupId = mDb.insert(GROUPS_TABLE_NAME, null, values);
            Log.v(TAG, "insertGroup() - inserted group with id: " + groupId);

            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
        }

        Log.v(TAG, "insertGroup() - end. );");

        return groupId;
    }

    public boolean removeGroup(long _rowIndex) {
        return mDb.delete(GROUPS_TABLE_NAME, Group.Columns._ID + "=" + _rowIndex, null) > 0;
    }

    public boolean updateGroup(long _groupId, Group _group) {
        boolean result = false;

        ContentValues values = new ContentValues();
        values.put(Group.Columns.NAME_COLUMN, _group.getName());
        values.put(Group.Columns.IS_READY, _group.isReady() ? 1 : 0);

        result = (mDb.update(GROUPS_TABLE_NAME, values, Group.Columns._ID + "=" + _groupId, null) > 0);

        return result;
    }

    public Group getGroupById(long _groupId) {
        Group group = null;
        Cursor cursor = mDb.query(
          GROUPS_TABLE_NAME,
          Group.Columns.ALL,
          Group.Columns._ID + " = " + _groupId,
          null,
          null,
          null,
          Group.Columns.DEFAULT_SORT_ORDER
        );

        if((cursor.getCount() == 0) || !cursor.moveToFirst()) {
            throw new SQLException("No Group item found for row: " + _groupId);
        }

        group = populateGroup(cursor);

        cursor.close();

        return group;
    }

    // TODO This shouldn't be necessary - just pass around the
    // groupId in the activities and you won't have to query this
    // repeatedly.
    public long getGroupByMemberId(long _memberId) {
        Cursor cursor = mDb.query(
          MEMBERS_TABLE_NAME,
          new String[]{ Member.Columns.GROUP_ID_COLUMN },
          Member.Columns._ID + " = " + _memberId,
          null,
          null,
          null,
          null
        );
        cursor.moveToFirst();

        return cursor.getLong(cursor.getColumnIndex(Member.Columns.GROUP_ID_COLUMN));
    }

    public List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<Group>();

        Cursor cursor = getAllGroupsCursor();

        while(cursor.moveToNext()) {
            Group group = populateGroup(cursor);
            groups.add(group);
        }
        cursor.close();

        return groups;
    }

    public Cursor getAllGroupsCursor() {

        Cursor cursor = mDb.query(
          GROUPS_TABLE_NAME,
          Group.Columns.ALL,
          null,
          null,
          null,
          null,
          Group.Columns.DEFAULT_SORT_ORDER
        );

        return cursor;
    }

    public boolean setGroupIsReady(long _groupId, boolean ready) {
        boolean result = false;

        ContentValues values = new ContentValues();
        values.put(Group.Columns.IS_READY, ready ? 1 : 0);

        result = (mDb.update(GROUPS_TABLE_NAME, values, Group.Columns._ID + "=" + _groupId, null) > 0);

        return result;
    }

    private Group populateGroup(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex((Group.Columns._ID)));
        String name = cursor.getString(cursor.getColumnIndex((Group.Columns.NAME_COLUMN)));
        boolean isReady = (cursor.getInt(cursor.getColumnIndex((Group.Columns.IS_READY))) == 1);

        Group group = new Group();
        group.setId(id);
        group.setName(name);
        group.setReady(isReady);

        return group;
    }

	/*
	 * Members
	 * 
	 */

    public long insertMember(long _groupId, Member _member) {

        // Create a new row of values to insert.
        ContentValues values = new ContentValues();
        Log.v(TAG, "########## Inserting member: " + _member.getName() + ":" + _member.getContactDetail());
        values.put(Member.Columns.LOOKUP_KEY, _member.getLookupKey());
        values.put(Member.Columns.NAME_COLUMN, _member.getName());
        values.put(Member.Columns.CONTACT_MODE_COLUMN, _member.getContactMode());
        values.put(Member.Columns.CONTACT_DETAIL_COLUMN, _member.getContactDetail());
        values.put(Member.Columns.GROUP_ID_COLUMN, _groupId);
        Log.v(TAG, "########## Inserting VALUE member: " + values.getAsString(Member.Columns.NAME_COLUMN) + ":" + values.getAsString(Columns.CONTACT_DETAIL_COLUMN));

        long mId = mDb.insert(MEMBERS_TABLE_NAME, null, values);

        return mId;
    }

    public boolean removeMember(long _memberId) {
        // Remove member
        return mDb.delete(
          MEMBERS_TABLE_NAME,
          Member.Columns._ID + " = " + _memberId,
          null
        ) > 0;
    }

    public boolean updateMember(long _rowId, Member _member) {
        boolean result = false;

        ContentValues values = new ContentValues();
        values.put(Member.Columns.LOOKUP_KEY, _member.getLookupKey());
        values.put(Columns.NAME_COLUMN, _member.getName());
        values.put(Member.Columns.CONTACT_MODE_COLUMN, _member.getContactMode());
        values.put(Member.Columns.CONTACT_DETAIL_COLUMN, _member.getContactDetail());

        result = (mDb.update(MEMBERS_TABLE_NAME, values, Member.Columns._ID + "=" + _rowId, null) > 0);

        return result;
    }

    public Member getMemberById(long _memberId) {

        Member member = null;
        Cursor cursor = mDb.query(
          MEMBERS_TABLE_NAME,
          Member.Columns.ALL,
          Member.Columns._ID + " = " + _memberId,
          null,
          null,
          null,
          Member.Columns.DEFAULT_SORT_ORDER
        );

        if((cursor.getCount() == 0) || !cursor.moveToFirst()) {
            throw new SQLException("No Member item found for row: " + _memberId);
        }

        member = populateMember(cursor);
        cursor.close();

        return member;
    }

    // Can't seem to specify composite primary key where one is the Autoincrement ID
    // so added this basically to check whether the entry exists already
    public Member getMember(long groupId, String memberName) {
        Cursor cursor = mDb.query(
          MEMBERS_TABLE_NAME,
          Member.Columns.ALL,
          Columns.NAME_COLUMN + " = ? and " + Columns.GROUP_ID_COLUMN + " = " + groupId,
          new String[]{ memberName },
          null,
          null,
          null
        );

        if((cursor.getCount() == 0) || !cursor.moveToFirst()) {
            cursor.close();
            return null;
        } else {
            Member member = populateMember(cursor);
            cursor.close();
            return member;
        }
    }

    public Cursor getAllMembersCursor(long _groupId) {

        Cursor cursor = mDb.query(
          MEMBERS_TABLE_NAME,
          Member.Columns.ALL,
          Member.Columns.GROUP_ID_COLUMN + " = " + _groupId,
          null,
          null,
          null,
          Member.Columns.DEFAULT_SORT_ORDER
        );

        return cursor;
    }

    /*
     * Convenience method - maps member name to member.
     *
     * Not really sure if you should use this, as it
     * uses the name as a key.
     */
    public Map<String, Member> getAllMembers(long _groupId) {
        Map<String, Member> members = new HashMap<String, Member>();

        Cursor cursor = getAllMembersCursor(_groupId);

        while(cursor.moveToNext()) {
            Member p = populateMember(cursor);
            members.put(p.getName(), p);
        }

        cursor.close();
        return members;
    }
//
//	/*
//	 * Another convenience method.
//	 * NOT USED - SO COMMENTING OUT.
//	 */
//	public boolean setMembers(long _groupId, Map<String, Member> _members)
//	{		
//
//		try {
//			mDb.beginTransaction();
//
//			// Remove old members for that Group id
//			mDb.delete(
//					MEMBERS_TABLE_NAME,
//					Columns.GROUP_ID_COLUMN + " = " + _groupId,
//					null
//			);
//
//			for (Member m: _members.values()) {
//
//				// Create a new row of values to insert.
//				ContentValues values = new ContentValues();
//
//				values.put(Columns.NAME_COLUMN, m.getName());
//				values.put(Columns.CONTACT_MODE_COLUMN, m.getContactMode());
//				values.put(Columns.CONTACT_DETAIL_COLUMN, m.getContactDetail());
//				values.put(Columns.GROUP_ID_COLUMN, _groupId);
//
//				long pId = mDb.insert(MEMBERS_TABLE_NAME, null, values);
//
//				// Changed from 0 - should return -1 from DB when failed.
//				if (pId == PersistableObject.UNSET_ID ) {
//					// This rollback all the other adds too.
//					Log.e(TAG, "setMembers() - Unable to add Member: " + m.getName() + " to Group Id: " + _groupId);
//					return false;
//				}
//
//			}
//			mDb.setTransactionSuccessful();
//			return true;
//
//		} finally {
//			mDb.endTransaction();
//		}
//	}

    private Member populateMember(Cursor cursor) {
        Member member = new Member();

        member.setId(cursor.getLong(cursor.getColumnIndex((Member.Columns._ID))));
        member.setLookupKey(cursor.getString(cursor.getColumnIndex((Member.Columns.LOOKUP_KEY))));
        member.setName(cursor.getString(cursor.getColumnIndex((Columns.NAME_COLUMN))));
        member.setContactDetail(cursor.getString(cursor.getColumnIndex((Member.Columns.CONTACT_DETAIL_COLUMN))));
        member.setContactMode(cursor.getInt(cursor.getColumnIndex((Columns.CONTACT_MODE_COLUMN))));

        return member;
    }

	/*
	 * Restrictions
	 */

    /*
     * Basic query - DOES NOT USE NAMES!
     */
    public Cursor getRestrictionsForMemberId(long _memberId) {
        Cursor cursor = mDb.query(
          RESTRICTIONS_TABLE_NAME,
          Restriction.Columns.ALL,
          Restriction.Columns.MEMBER_ID_COLUMN + " = " + _memberId,
          null,
          null,
          null,
          Restriction.Columns.DEFAULT_SORT_ORDER
        );

        return cursor;
    }

	/*
	 * 
	 */

    public Cursor getRestrictionNamesForMemberIdCursor(long _memberId) {
        String query = getMemberRestrictionsSQL(_memberId);
        Log.v(TAG, query);
        return mDb.rawQuery(query, null);
    }

    private String getMemberRestrictionsSQL(long _memberId) {
        String subQuery = SQLiteQueryBuilder.buildQueryString(true, RESTRICTIONS_TABLE_NAME, new String[]{ Restriction.Columns.OTHER_MEMBER_ID_COLUMN },
          _memberId + " = " +  Restriction.Columns.MEMBER_ID_COLUMN, null, null, null, null);
        String restrictionsQuery = SQLiteQueryBuilder.buildQueryString(true, MEMBERS_TABLE_NAME, new String[]{ Member.Columns.NAME_COLUMN, Member.Columns._ID },
          Columns._ID + " IN (" + subQuery + ")", null, null, null, null);

        Log.v(TAG, subQuery);
        Log.v(TAG, restrictionsQuery);

        return restrictionsQuery;
    }

    //	public List<String> getAllRestrictionNamesForMemberId(long _memberId)
    //	{
    //
    //		// Don't care about order, they're just Ids
    //		// Should I do a join to get the Name and just use that?
    //		ArrayList<String> restrictions = new ArrayList<String>();
    //
    //		Cursor cursor = getRestrictionNameForMemberIdCursor(_memberId);
    //		while (cursor.moveToNext()) {
    //			//  A member's name is unique for a group.
    //			String other = cursor.getString(cursor.getColumnIndex((Columns.NAME_COLUMN)));
    //			restrictions.add(other);
    //		}
    //
    //		cursor.close();
    //
    //		return restrictions;
    //	}

    public Cursor getOtherGroupMembers(long _memberId) {
        long _groupId = getGroupByMemberId(_memberId);
        String otherGroupMembersQuery = SQLiteQueryBuilder.buildQueryString(true, MEMBERS_TABLE_NAME, new String[]{ Member.Columns.NAME_COLUMN, Columns._ID },
          _groupId + " = " + Member.Columns.GROUP_ID_COLUMN + " and " + _memberId + " != " + Member.Columns._ID, null, null, MEMBERS_TABLE_NAME + "."
          + Member.Columns.NAME_COLUMN + " ASC", null);

        return mDb.rawQuery(otherGroupMembersQuery, null);
    }

    public long insertRestriction(long _memberId, long _otherMemberId) {
        Log.v(TAG, "insertRestriction() - start");

        ContentValues values = new ContentValues();

        values.put(Restriction.Columns.MEMBER_ID_COLUMN, _memberId);
        values.put(Restriction.Columns.OTHER_MEMBER_ID_COLUMN, _otherMemberId);

        long rowId = mDb.insert(RESTRICTIONS_TABLE_NAME, null, values);

        Log.v(TAG, "insertRestriction() - inserted restriction with: " + rowId);

        return rowId;
    }

    public boolean removeRestriction(long _restrictionId) {
        // Remove restriction
        return mDb.delete(
          RESTRICTIONS_TABLE_NAME,
          Restriction.Columns._ID + " = " + _restrictionId,
          null
        ) > 0;
    }

    public boolean removeAllRestrictionsForMember(long _memberId) {
        // Remove restriction
        return mDb.delete(
          RESTRICTIONS_TABLE_NAME,
          Restriction.Columns.MEMBER_ID_COLUMN + " = " + _memberId,
          null
        ) > 0;
    }

    public boolean removeRestrictionByMemberId(long _memberId, long _restrictedMemberId) {
        Log.v(TAG, "removeRestrictionByMemberId " + _memberId + " : " + _restrictedMemberId);
        // Remove restriction
        return mDb.delete(
          RESTRICTIONS_TABLE_NAME,
          Restriction.Columns.MEMBER_ID_COLUMN + " = " + _memberId + " and " +
            Restriction.Columns.OTHER_MEMBER_ID_COLUMN + " = " + _restrictedMemberId,
          null
        ) > 0;
    }

	/*
	 * Can't imagine this actually getting used - so commenting out.
	 */
    //	public boolean updateRestriction(long _restrictionId, long _memberId, long _otherMemberId)
    //	{
    //		boolean result = false;
    //
    //		ContentValues values = new ContentValues();
    //		values.put(RestrictionsColumns.MEMBER_ID_COLUMN, _memberId);
    //		values.put(RestrictionsColumns.OTHER_MEMBER_ID_COLUMN, _otherMemberId);
    //
    //		result = (mDb.update(RESTRICTIONS_TABLE_NAME, values, RestrictionsColumns._ID + "=" + _restrictionId, null) > 0);
    //
    //		return result;
    //	}

    public Cursor getRestrictionByIdCursor(long _restrictionId) {

        Cursor cursor = mDb.query(
          RESTRICTIONS_TABLE_NAME,
          Restriction.Columns.ALL,
          Restriction.Columns._ID + " = " + _restrictionId,
          null,
          null,
          null,
          Restriction.Columns.DEFAULT_SORT_ORDER
        );

        if((cursor.getCount() == 0) || !cursor.moveToFirst()) {
            throw new SQLException("No Restriction item found for row: " + _restrictionId);
        }

        return cursor;
    }

	/*
	 * Draw Results
	 * 
	 * Fairly primitive, so not much in the way of convenience methods.
	 */

    public long insertDrawResult(DrawResult _drawResult, long _groupId) {
        // Create a new row of values to insert.
        ContentValues values = new ContentValues();

        values.put(DrawResult.Columns.DRAW_DATE_COLUMN, _drawResult.getDrawDate());
        values.put(DrawResult.Columns.GROUP_ID_COLUMN, _groupId);
        values.put(DrawResult.Columns.SEND_DATE_COLUMN, _drawResult.getSendDate());
        values.put(DrawResult.Columns.MESSAGE_COLUMN, _drawResult.getMessage());

        long drawResultId = mDb.insert(DRAW_RESULTS_TABLE_NAME, null, values);

        return drawResultId;
    }

    public boolean removeDrawResult(long _rowIndex) {
        return mDb.delete(DRAW_RESULTS_TABLE_NAME, DrawResult.Columns._ID + "=" + _rowIndex, null) > 0;
    }

    /*
     * Can't see anyone using this, date only really set on creation.
     */
    public boolean updateDrawResult(DrawResult _drawResult, long _rowIndex) {

        ContentValues values = new ContentValues();
        values.put(DrawResult.Columns._ID, _rowIndex);
        values.put(DrawResult.Columns.DRAW_DATE_COLUMN, _drawResult.getDrawDate());
        values.put(DrawResult.Columns.SEND_DATE_COLUMN, _drawResult.getSendDate());
        values.put(DrawResult.Columns.MESSAGE_COLUMN, _drawResult.getMessage());

        return mDb.update(DRAW_RESULTS_TABLE_NAME, values, DrawResult.Columns._ID + "=" + _rowIndex, null) > 0;
    }

    public boolean setDrawSharedDate(long _drawResultId, long time) {
        ContentValues values = new ContentValues();
        values.put(DrawResult.Columns.SEND_DATE_COLUMN, time);
        return mDb.update(DRAW_RESULTS_TABLE_NAME, values, DrawResult.Columns._ID + "=" + _drawResultId, null) > 0;
    }

    public DrawResult getDrawResultById(long _drawResultId) {
        DrawResult drawResult = null;
        Cursor cursor = mDb.query(
          DRAW_RESULTS_TABLE_NAME,
          DrawResult.Columns.ALL,
          DrawResult.Columns._ID + " = " + _drawResultId,
          null,
          null,
          null,
          DrawResult.Columns.DEFAULT_SORT_ORDER
        );

        if((cursor.getCount() == 0) || !cursor.moveToFirst()) {
            throw new SQLException("No Draw Result item found for row: " + _drawResultId);
        }
        drawResult = populateDrawResult(cursor);

        cursor.close();

        return drawResult;
    }

    private DrawResult populateDrawResult(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex((DrawResult.Columns._ID)));
        long ddate = cursor.getLong(cursor.getColumnIndex((DrawResult.Columns.DRAW_DATE_COLUMN)));
        long sdate = cursor.getLong(cursor.getColumnIndex((DrawResult.Columns.SEND_DATE_COLUMN)));
        String msg = cursor.getString(cursor.getColumnIndex((DrawResult.Columns.MESSAGE_COLUMN)));

        DrawResult dr = new DrawResult();
        dr.setId(id);
        dr.setDrawDate(ddate);
        dr.setSendDate(sdate);
        dr.setMessage(msg);

        return dr;
    }

    public Cursor getAllDrawResultsForGroupCursor(long _groupId) {
        Cursor cursor = mDb.query(
          DRAW_RESULTS_TABLE_NAME,
          DrawResult.Columns.ALL,
          DrawResult.Columns.GROUP_ID_COLUMN + " = " + _groupId,
          null,
          null,
          null,
          DrawResult.Columns.DEFAULT_SORT_ORDER
        );

        return cursor;
    }

    public Cursor getAllDrawResultsCursor() {
        Cursor cursor = mDb.query(
          DRAW_RESULTS_TABLE_NAME,
          DrawResult.Columns.ALL,
          null,
          null,
          null,
          null,
          DrawResult.Columns.DEFAULT_SORT_ORDER
        );

        return cursor;
    }

    /*
     * Draw Result Entries.
     *
     * These are pretty primitive, so no convenience methods for them... for now.
     */
    public long insertDrawResultEntry(long _drawResultId, DrawResultEntry _drawResultEntry) {
        ContentValues values = new ContentValues();
        values.put(DrawResultEntry.Columns.DRAW_RESULT_ID_COLUMN, _drawResultId);
        values.put(DrawResultEntry.Columns.MEMBER_NAME_COLUMN, _drawResultEntry.getGiverName());
        values.put(DrawResultEntry.Columns.OTHER_MEMBER_NAME_COLUMN, _drawResultEntry.getReceiverName());
        values.put(DrawResultEntry.Columns.CONTACT_MODE_COLUMN, _drawResultEntry.getContactMode());
        values.put(DrawResultEntry.Columns.CONTACT_DETAIL_COLUMN, _drawResultEntry.getContactDetail());
        values.put(DrawResultEntry.Columns.VIEWED_DATE_COLUMN, _drawResultEntry.getViewedDate());

        return mDb.insert(DRAW_RESULT_ENTRIES_TABLE_NAME, null, values);
    }

    public Cursor getAllDrawResultEntriesForDrawIdCursor(long _drawResultId) {

        Cursor cursor = mDb.query(
          DRAW_RESULT_ENTRIES_TABLE_NAME,
          DrawResultEntry.Columns.ALL,
          DrawResultEntry.Columns.DRAW_RESULT_ID_COLUMN + " = " + _drawResultId,
          null,
          null,
          null,
          DrawResultEntry.Columns.DEFAULT_SORT_ORDER
        );

        return cursor;
    }

    private DrawResultEntry populateDrawResultEntry(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex((DrawResultEntry.Columns._ID)));
        String name1 = cursor.getString(cursor.getColumnIndex(DrawResultEntry.Columns.MEMBER_NAME_COLUMN));
        String name2 = cursor.getString(cursor.getColumnIndex(DrawResultEntry.Columns.OTHER_MEMBER_NAME_COLUMN));
        int contactMode = cursor.getInt(cursor.getColumnIndex(DrawResultEntry.Columns.CONTACT_MODE_COLUMN));
        String contactDetail = cursor.getString(cursor.getColumnIndex(DrawResultEntry.Columns.CONTACT_DETAIL_COLUMN));
        long viewedDate = cursor.getLong(cursor.getColumnIndex(DrawResultEntry.Columns.VIEWED_DATE_COLUMN));
        long sentDate = cursor.getLong(cursor.getColumnIndex(DrawResultEntry.Columns.SENT_DATE_COLUMN));

        DrawResultEntry dre = new DrawResultEntry();
        dre.setGiverName(name1);
        dre.setReceiverName(name2);
        dre.setContactMode(contactMode);
        dre.setContactDetail(contactDetail);
        dre.setViewedDate(viewedDate);
        dre.setSentDate(sentDate);

        Log.v(TAG, "############### populateDrawResultEntry: " + name1 + " | " + name2 + "| " + contactDetail);
        dre.setId(id);
        return dre;
    }

    public List<DrawResultEntry> getAllDrawResultEntriesForDrawId(long _drawResultId) {
        List<DrawResultEntry> result = new ArrayList<DrawResultEntry>();

        Cursor cursor = getAllDrawResultEntriesForDrawIdCursor(_drawResultId);
        while(cursor.moveToNext()) {
            result.add(populateDrawResultEntry(cursor));
        }
        cursor.close();

        return result;
    }

    public boolean removeDrawResultEntry(long _drawResultEntryId) {
        // Remove draw result entry
        // User can't actually do this...
        return mDb.delete(
          DRAW_RESULT_ENTRIES_TABLE_NAME,
          DrawResultEntry.Columns._ID + " = " + _drawResultEntryId,
          null
        ) > 0;
    }

    public boolean updateDrawResultEntry(DrawResultEntry _drawResultEntry, long _rowIndex) {

        ContentValues values = new ContentValues();
        values.put(DrawResultEntry.Columns.MEMBER_NAME_COLUMN, _drawResultEntry.getGiverName());
        values.put(DrawResultEntry.Columns.OTHER_MEMBER_NAME_COLUMN, _drawResultEntry.getReceiverName());
        values.put(DrawResultEntry.Columns.CONTACT_MODE_COLUMN, _drawResultEntry.getContactMode());
        values.put(DrawResultEntry.Columns.CONTACT_DETAIL_COLUMN, _drawResultEntry.getContactDetail());
        values.put(DrawResultEntry.Columns.VIEWED_DATE_COLUMN, _drawResultEntry.getViewedDate());
        values.put(DrawResultEntry.Columns.SENT_DATE_COLUMN, _drawResultEntry.getSentDate());

        return mDb.update(DRAW_RESULT_ENTRIES_TABLE_NAME, values, DrawResultEntry.Columns._ID + "=" + _rowIndex, null) > 0;
    }

    public long getLatestDrawResultId(long _groupId) {

        // Changed from 0
        long result = PersistableObject.UNSET_ID;

        Cursor cursor = mDb.rawQuery(getLatestDrawResultSQL(_groupId), new String[]{ });

        if(cursor.moveToFirst())
            result = cursor.getLong(cursor.getColumnIndex(DrawResult.Columns._ID));

        cursor.close();
        return result;
    }

    private String getLatestDrawResultSQL(long _groupId) {
        // Have to kinda fudge the column here.
        String subQuery = SQLiteQueryBuilder.buildQueryString(true, DRAW_RESULTS_TABLE_NAME, new String[]{ "max(" + DrawResult.Columns.DRAW_DATE_COLUMN + ")" },
          DrawResult.Columns.GROUP_ID_COLUMN + " = " + _groupId, null, null, null, null);
        String restrictionsQuery = SQLiteQueryBuilder.buildQueryString(true, DRAW_RESULTS_TABLE_NAME, new String[]{ DrawResult.Columns._ID },
          DrawResult.Columns.DRAW_DATE_COLUMN + " IN (" + subQuery + ")", null, null, null, null);

        Log.v(TAG, subQuery);
        Log.v(TAG, restrictionsQuery);

        return restrictionsQuery;
    }

    /*
     * Test Methods
     */
    public SQLiteDatabase getDBImpl() {
        return mDb;
    }
}

