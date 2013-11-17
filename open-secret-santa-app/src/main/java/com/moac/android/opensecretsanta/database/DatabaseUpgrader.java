package com.moac.android.opensecretsanta.database;

import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.ContactMethod;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.version2.DrawResultEntryVersion2;
import com.moac.android.opensecretsanta.model.version2.DrawResultVersion2;
import com.moac.android.opensecretsanta.model.version2.GroupVersion2;
import com.moac.android.opensecretsanta.model.version2.MemberVersion2;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: amelysh
 * Date: 15.11.13
 * Time: 22:06
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseUpgrader {

    private static final String TAG = DatabaseUpgrader.class.getSimpleName();
    private DatabaseHelper mDbHelper;

    public DatabaseUpgrader(DatabaseHelper mDbHelper) {
        this.mDbHelper = mDbHelper;
    }

    public void upgradeDatabaseToVersion3() {
        alterMemberTable();
        alterGroupTable();
        migrateDataToVersion3AssignmentsTable();
    }

    /***************************************************************
     *
     *  Database schema changes
     *
     **************************************************************/
    protected void alterMemberTable() {
        try {
          // Member table now has an extra contact_id.
          mDbHelper.getDaoEx(MemberVersion2.class).executeRaw("ALTER TABLE '" + MemberVersion2.TABLE_NAME + "' ADD COLUMN " + Member.Columns.CONTACT_ID + " INTEGER;");

          // we are now using the ContactMethod enum,
          // hmmmmm you cannot DROP a column
          // so let's add a new column called CONTACT_METHOD
          // as we don't need it for old members being migrated anyway we can just set the default to ContactMethod.REVEAL_ONLY for old members
          mDbHelper.getDaoEx(MemberVersion2.class).executeRaw("ALTER TABLE '" + MemberVersion2.TABLE_NAME + "' ADD COLUMN " + Member.Columns.CONTACT_METHOD_COLUMN + " STRING NOT NULL DEFAULT " + ContactMethod.REVEAL_ONLY);

        } catch (SQLException e) {
            throw new android.database.SQLException(e.getMessage());
        }
    }

    protected void alterGroupTable() {
        try {
            // Group table now has extra message, created_at, draw_date and is dropping isReady but as we can't drop a column, we'll just leave it
            // but remove the reference in Group.java
            mDbHelper.getDaoEx(Group.class).executeRaw("ALTER TABLE '" + GroupVersion2.TABLE_NAME + "' ADD COLUMN " + Group.Columns.MESSAGE_COLUMN + " STRING;");
            mDbHelper.getDaoEx(Group.class).executeRaw("ALTER TABLE '" + GroupVersion2.TABLE_NAME + "' ADD COLUMN " + Group.Columns.CREATED_AT_COLUMN + " INTEGER;");
            mDbHelper.getDaoEx(Group.class).executeRaw("ALTER TABLE '" + GroupVersion2.TABLE_NAME + "' ADD COLUMN " + Group.Columns.DRAW_DATE_COLUMN + " INTEGER;");
        } catch (SQLException e) {
            throw new android.database.SQLException(e.getMessage());
        }
    }



    /***************************************************************
     *
     *  Database content changes
     *
     **************************************************************/

    // The trickiest one is building the Assignment table
    // i) for each group, get all the Draw Results
    //    delete that group entry
    //    and with each Draw Result, add a new entry as a new Group (name is group + draw date)
    //    so each old Draw Results will now be a new Group (new schema rules)
    //    - migrate the message from Draw Result to the Group
    //    - migrate the draw date from Draw Result to the Group
    //    - set Group created date = draw date
    protected void migrateDataToVersion3AssignmentsTable() {
        List<GroupVersion2> groupsVersion2 = mDbHelper.queryAll(GroupVersion2.class);
        for (GroupVersion2 groupVersion2 : groupsVersion2) {
            List<DrawResultVersion2> drawResultsVersion2 = getAllDrawResultsVersion2ForGroup(groupVersion2.getId());
            for (DrawResultVersion2 drawResultVersion2 : drawResultsVersion2) {
                insertMigratedGroupInfo(drawResultVersion2, groupVersion2.getName());
                // for each Draw Result Entry, build the entry for the new Assignment
                //    Assignment columns are
                //    - giver member id and receiver member id
                //     (you need to get the member id from the given member name in the Member table)
                //    - send status
                //     (work it out from the Draw Result Entry sent and/or viewed date, if not set to assigned)
                //
                List<DrawResultEntryVersion2> drawResultEntriesVersion2 = mDbHelper.queryAll(DrawResultEntryVersion2.class);
                for (DrawResultEntryVersion2 drawResultEntryVersion2 : drawResultEntriesVersion2) {
                    insertMigratedAssignmentEntryInfo(drawResultEntryVersion2, groupVersion2.getId());
                }
            }

            // remove old group
            removeGroupVersion2(groupVersion2.getId());
        }
    }

    protected void removeGroupVersion2(long version2GroupId) {
        mDbHelper.getWritableDatabase().delete(Group.TABLE_NAME, Group.Columns._ID + " = ?",
                new String[] { String.valueOf(version2GroupId) });
    }

    protected void insertMigratedGroupInfo(DrawResultVersion2 drawResultVersion2, String version2GroupName) {
        try {
            Group migratedGroup = new Group();
            migratedGroup.setName("migrated " + version2GroupName + " " + drawResultVersion2.getDrawDate());
            migratedGroup.setMessage(drawResultVersion2.getMessage());
            migratedGroup.setDrawDate(drawResultVersion2.getDrawDate());

            // this is  new field and we don't have old data for that, so we will set created date = draw date
            migratedGroup.setCreatedAt(drawResultVersion2.getDrawDate());

            mDbHelper.getDaoEx(Group.class).create(migratedGroup);

        } catch (SQLException e) {
            throw new android.database.SQLException(e.getMessage());
        }
    }

    protected void insertMigratedAssignmentEntryInfo(DrawResultEntryVersion2 drawResultEntryVersion2, long groupId) {
        long giverMemberId = getMemberIdFromMemberName(drawResultEntryVersion2.getGiverName(), groupId);
        long receiverMemberId = getMemberIdFromMemberName(drawResultEntryVersion2.getReceiverName(), groupId);

        try {
            Assignment newAssignment = new Assignment();

            // foreign key being Member, we need to query for the Member object to set it
            Member giverMember = mDbHelper.getDaoEx(Member.class).queryForId(giverMemberId);
            newAssignment.setGiverMember(giverMember);

            Member receiverMember = mDbHelper.getDaoEx(Member.class).queryForId(receiverMemberId);
            newAssignment.setReceiverMember(receiverMember);

            Assignment.Status sendStatus = Assignment.Status.Assigned; // default
            if (drawResultEntryVersion2.getViewedDate() > 0) {
                sendStatus = Assignment.Status.Revealed;
            }
            else if (drawResultEntryVersion2.getSentDate() > 0) {
                sendStatus = Assignment.Status.Sent;
            }
            newAssignment.setSendStatus(sendStatus);

            mDbHelper.getDaoEx(Assignment.class).create(newAssignment);

        } catch (SQLException e) {
            throw new android.database.SQLException(e.getMessage());
        }
    }

    /***************************************************************
     *
     *  Database queries helper methods
     *
     **************************************************************/


    protected List<DrawResultVersion2> getAllDrawResultsVersion2ForGroup(long version2GroupId) {
        try {
            return mDbHelper.getDaoEx(DrawResultVersion2.class).queryBuilder()
                    .where().eq(DrawResultVersion2.Columns.GROUP_ID_COLUMN, version2GroupId)
                    .query();
        } catch (SQLException e) {
            throw new android.database.SQLException(e.getMessage());
        }
    }

    // if the member doesn't exist, it creates an entry
    protected long getMemberIdFromMemberName(String memberName, long groupId) {
        try {
            MemberVersion2 memberVersion2 = mDbHelper.getDaoEx(MemberVersion2.class).queryBuilder()
                                                  .where().eq(MemberVersion2.Columns.NAME_COLUMN, memberName).and()
                                                          .eq(MemberVersion2.Columns.GROUP_ID_COLUMN, groupId)
                                                 .queryForFirst();
            if (memberVersion2 != null) {
                return memberVersion2.getId();
            } else {
                return createMemberEntry(memberName, groupId);
            }
        } catch (SQLException e) {
            throw new android.database.SQLException(e.getMessage());
        }
    }

    // In our old schema, one could delete a member so there might be Draw Result Entry with a member name
    // that does not exist. As we are enforcing a relationship in our new schema such that an Assignment will
    // reference a Member, we have to create a dummy Member entry with a name and group id (the only two properties we have)
    private long createMemberEntry(String memberName, long groupId) {
        Member newMember = new Member();
        newMember.setName(memberName);
        newMember.setContactMethod(ContactMethod.REVEAL_ONLY);

        try {
            // we need the Group object to set it in the Member, so let's query the db
            Group group = mDbHelper.getDaoEx(Group.class).queryForId(groupId);
            newMember.setGroup(group);
            newMember.setGroup(new Group());
            return mDbHelper.getDaoEx(Member.class).create(newMember);
        } catch (SQLException e) {
            throw new android.database.SQLException(e.getMessage());
        }
    }

    /*protected List<GroupVersion2> getAllGroupsVersion2() {
        List<GroupVersion2> groupsVersion2 = new ArrayList<GroupVersion2>();
        Cursor cursor = db.query(GroupVersion2.TABLE_NAME,
                GroupVersion2.Columns.ALL, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            GroupVersion2 groupVersion2 = cursorToGroupVersion2(cursor);
            groupsVersion2.add(groupVersion2);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        return groupsVersion2;
    }

    private GroupVersion2 cursorToGroupVersion2(Cursor cursor) {
        GroupVersion2 groupVersion2 = new GroupVersion2();
        groupVersion2.setName(cursor.getString(cursor.getColumnIndex(GroupVersion2.Columns.NAME_COLUMN)));
        groupVersion2.setReady(cursor.getInt(cursor.getColumnIndex(GroupVersion2.Columns.IS_READY)) == 1);
        return groupVersion2;
    }

    private List<DrawResultVersion2> getAllDrawResultsVersion2ForGroup(long version2GroupId) {
        // get the most recent draw result based on Draw Date
        String selectQuery = "SELECT  * FROM " + DrawResultVersion2.TABLE_NAME + " WHERE "
                + DrawResultVersion2.Columns.GROUP_ID_COLUMN + " = " + version2GroupId;

        Log.i(TAG, selectQuery);
        List<DrawResultVersion2> drawResultsVersion2 = new ArrayList<DrawResultVersion2>();

        Cursor c = db.rawQuery(selectQuery, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            DrawResultVersion2 drawResultVersion2 = cursorToDrawResultVersion2(c);
            drawResultsVersion2.add(drawResultVersion2);
            c.moveToNext();
        }

        return drawResultsVersion2;
    }

    private DrawResultVersion2 cursorToDrawResultVersion2(Cursor c) {
        DrawResultVersion2 drawResultVersion2 = new DrawResultVersion2();
        drawResultVersion2.setDrawDate(c.getLong(c.getColumnIndex(DrawResultVersion2.Columns.DRAW_DATE_COLUMN)));
        drawResultVersion2.setSendDate(c.getLong(c.getColumnIndex(DrawResultVersion2.Columns.SEND_DATE_COLUMN)));
        drawResultVersion2.setMessage(c.getString(c.getColumnIndex(DrawResultVersion2.Columns.MESSAGE_COLUMN)));
        return drawResultVersion2;
    }

    private List<DrawResultEntryVersion2> getAllDrawResultEntriesVersion2() {
        List<DrawResultEntryVersion2> drawResultEntriesVersion2 = new ArrayList<DrawResultEntryVersion2>();
        Cursor cursor = db.query(DrawResultEntryVersion2.TABLE_NAME,
                DrawResultEntryVersion2.Columns.ALL, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DrawResultEntryVersion2 drawResultEntryVersion2 = cursorToDrawResultEntryVersion2(cursor);
            drawResultEntriesVersion2.add(drawResultEntryVersion2);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        return drawResultEntriesVersion2;
    }

    private DrawResultEntryVersion2 cursorToDrawResultEntryVersion2(Cursor cursor) {
        DrawResultEntryVersion2 drawResultEntryVersion2 = new DrawResultEntryVersion2();
        drawResultEntryVersion2.setGiverName(cursor.getString(cursor.getColumnIndex(DrawResultEntryVersion2.Columns.MEMBER_NAME_COLUMN)));
        drawResultEntryVersion2.setReceiverName(cursor.getString(cursor.getColumnIndex(DrawResultEntryVersion2.Columns.OTHER_MEMBER_NAME_COLUMN)));
        drawResultEntryVersion2.setContactDetail(cursor.getString(cursor.getColumnIndex(DrawResultEntryVersion2.Columns.CONTACT_DETAIL_COLUMN)));
        drawResultEntryVersion2.setViewedDate(cursor.getLong(cursor.getColumnIndex(DrawResultEntryVersion2.Columns.VIEWED_DATE_COLUMN)));
        drawResultEntryVersion2.setSentDate(cursor.getLong(cursor.getColumnIndex(DrawResultEntryVersion2.Columns.SENT_DATE_COLUMN)));
        return drawResultEntryVersion2;
    }

    private int getMemberIdFromMemberName(String memberName) {
        // get the most recent draw result based on Draw Date
        String selectQuery = "SELECT " + MemberVersion2.Columns._ID + " FROM " + MemberVersion2.TABLE_NAME + " WHERE "
                + MemberVersion2.Columns.NAME_COLUMN + " = " + memberName;

        Log.i(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);
        c.moveToFirst();
        return c.getInt(c.getColumnIndex(MemberVersion2.Columns._ID));
    }    */
}
