package com.moac.android.opensecretsanta.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.moac.android.opensecretsanta.model.*;
import com.moac.android.opensecretsanta.model.version2.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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


    // One flaw in our database upgrade that is limited by the old schema
    // in that we didn't store the restrictions per draw result entry
    // therefore we will not show any restrictions for past draw result
    // WARNING to users: For groups with one or more draw result, we
    // can only show the last restrictions set on the last group action
    // (be it a 'build group' or 'draw')
    public DatabaseUpgrader(DatabaseHelper mDbHelper) {
        this.mDbHelper = mDbHelper;
    }

    public void upgradeDatabaseSchemaToVersion3(ConnectionSource cs) {
        //sets up required tables for version 3
        addNewAssignmentTable(cs);

        renameMemberVersion2Table(cs);
        createNewMemberVersion3Table(cs);

        renameRestrictionsVersion2Table(cs);
        createNewRestrictionsVersion3Table(cs);

        alterGroupTable();
    }

    public void migrateDataToVersion3(SQLiteDatabase db, ConnectionSource cs) {
        boolean success = true;
        // TODO figure out why we can't have two independent transactions.
        // rolling back this second one in the migration data part seemed to roll back
        // the schema change too, so disabled it for now. too bad
        //db.beginTransaction();
        success = migrateMemberAndRestrictionsTable(cs);

//        if (success) {
//            db.setTransactionSuccessful();
//        }
//
//        db.endTransaction();

        if (success) {
            // only makes sense to continue migration of members and restrictions were migrated succesfully
            migrateDataToVersion3AssignmentsTable(db);
        }
    }

    /***************************************************************
     *
     *  Database schema changes
     *
     **************************************************************/
    protected void addNewAssignmentTable(ConnectionSource cs) {
        Log.d(TAG, "addNewAssignmentTable");
        try {
            TableUtils.createTable(cs, Assignment.class);
        } catch(java.sql.SQLException e) {
            throw new android.database.SQLException(e.getMessage());
        }
    }

    // we move this in a different function to allow for testability
    private void renameMemberVersion2Table(ConnectionSource cs) {
        try {
            // we are now using the ContactMethod enum instead of ContactMode
            // hmmmmm you cannot DROP a column
            // nor change the constraint we have on enforcing ContactMode

            // workaround, let's rename old table
            mDbHelper.getDaoEx(MemberVersion2.class).executeRaw("ALTER TABLE '" + MemberVersion2.VERSION2_TABLE_NAME + "' RENAME TO " + MemberVersion2.TABLE_NAME);

        } catch (SQLException e) {
            throw new android.database.SQLException(e.getMessage());
        }
    }

    protected void createNewMemberVersion3Table(ConnectionSource cs) {
        try {
            // create new Member table
            TableUtils.createTable(cs, Member.class);
        } catch (SQLException e) {
            throw new android.database.SQLException(e.getMessage());
        }
    }

    protected void renameRestrictionsVersion2Table(ConnectionSource cs) {
        try {
            // we need to keep two restrictions table to allow for migration without
            // the tables stepping on each others' feet
            mDbHelper.getDaoEx(RestrictionVersion2.class).executeRaw("ALTER TABLE '" + RestrictionVersion2.VERSION2_TABLE_NAME + "' RENAME TO " + RestrictionVersion2.TABLE_NAME);

        } catch (SQLException e) {
            throw new android.database.SQLException(e.getMessage());
        }
    }

    protected void createNewRestrictionsVersion3Table(ConnectionSource cs) {
        try {
            // create new Restriction table
            TableUtils.createTable(cs, Restriction.class);
        } catch (SQLException e) {
            throw new android.database.SQLException(e.getMessage());
        }
    }

    // we copy all data across to new table
    protected boolean migrateMemberAndRestrictionsTable(ConnectionSource cs) {
        Log.d(TAG, "migrateMemberAndRestrictionsTable");
        try {

            // we store the all id and the new member. we need the old id so that we can look up which restriction is needed
            Map storedMapping = new HashMap();

            // copy all data from old Member to new Member
            List<MemberVersion2> allMembersVersion2 = mDbHelper.queryAll(MemberVersion2.class);
            Log.d(TAG, "allMembersVersion2 " + allMembersVersion2.size());
            for (MemberVersion2 memberVersion2 : allMembersVersion2) {
                Log.d(TAG, "memberVersion2 " + memberVersion2.getName());
                Member migratedMember = convertMemberToVersion3(memberVersion2);
                mDbHelper.create(migratedMember, Member.class);

                Log.d(TAG, "storedMapping for migrated member key: " + memberVersion2.getId() + " value: " + memberVersion2.getName());
                storedMapping.put(new Long(memberVersion2.getId()), migratedMember);
            }

            // now that we have migrated all the members
            // let's go through and work through the restrictions
            Iterator it = storedMapping.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry pairs = (Map.Entry) it.next();
                long oldMemberId = (Long) pairs.getKey();
                Log.d(TAG, "---------------- oldMemberId: " + oldMemberId);
                List<RestrictionVersion2> oldRestrictions = mDbHelper.getDaoEx(RestrictionVersion2.class).queryBuilder()
                        .where().eq(RestrictionVersion2.Columns.MEMBER_ID_COLUMN, oldMemberId)
                        .query();

                for (RestrictionVersion2 oldRestriction : oldRestrictions) {
                    Log.d(TAG, "old restriction: " + oldRestriction.getId() + " member: "  + oldRestriction.getMemberId() + " other:" + oldRestriction.getOtherMemberId());
                    // with the old restriction member id, we can find out the name
                    MemberVersion2 memberRequiredForName =  mDbHelper.queryById(oldRestriction.getOtherMemberId(), MemberVersion2.class);

                    long groupId = ((Member)pairs.getValue()).getGroupId();
                    Log.d(TAG, "memberRequiredForName name: " + memberRequiredForName.getName() + " group:" + groupId);

                    SelectArg selectArg = new SelectArg();
                    selectArg.setValue( memberRequiredForName.getName());
                    // with the name and the new group id, we can find the new Member
                    List<Member> tmpMembersList = mDbHelper.getDaoEx(Member.class).queryBuilder()
                            .where().eq(Member.Columns.NAME_COLUMN, selectArg).and()
                            .eq(Member.Columns.GROUP_ID_COLUMN, groupId)
                            .query();

                    Log.d(TAG, "query back:" + tmpMembersList.get(0).getName() + " id:" + tmpMembersList.get(0).getId() + " groupId: " + tmpMembersList.get(0).getGroupId());

                    // there should only be one member but we need to get a list back

                    // we can then use the name to look for the new member id of the restriction
                    // restriction will have the member id (the value in the storedMapping) and
                    //                           other member id (id from first element of tmpMember)
                    // and insert it! phew
                    Restriction.Builder rBuilder = new Restriction.Builder();
                    Restriction newMigratedRestriction = rBuilder.withMember((Member)pairs.getValue())
                                                                 .withOtherMember(tmpMembersList.get(0))
                                                                 .build();
                    Log.d(TAG, " New migrated restriction : "  + newMigratedRestriction.getMemberId() + " other: " + newMigratedRestriction.getOtherMemberId());
                    mDbHelper.create(newMigratedRestriction, Restriction.class);
                }
                it.remove();
                Log.d(TAG, "---------------------------");
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception in migrateMemberAndRestrictionsTable. Migration will abort and rollback. Exception: " + e.getMessage());
            return false;
        }

        Log.d(TAG, "migrateMemberAndRestrictionsTable returning true");
        return true;
    }

    protected void alterGroupTable() {
        Log.d(TAG, "alterGroupTable");
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

    public void dropOldVersion2Tables(ConnectionSource cs) {
        try {
            Log.d(TAG, "dropOldVersion2Tables");

            // we can ignore errors, since we can't handle the exception anyway

            // from the old schema, we copied the Members table, Restrictions table for
            // migration purposes and now we can drop those temporary tables
            TableUtils.dropTable(cs, MemberVersion2.class, true);
            TableUtils.dropTable(cs, RestrictionVersion2.class, true);

            // we don't need DrawResults and DrawResultEntry anymore, so we can drop them
            TableUtils.dropTable(cs, DrawResultVersion2.class, true);
            TableUtils.dropTable(cs, DrawResultEntryVersion2.class, true);

            // we don't touch the Group table as we are still using it and updated it
            // during the migration

        } catch (Exception e) {

        }
    }



    /***************************************************************
     *
     *  Database content changes
     *
     **************************************************************/
    protected Member convertMemberToVersion3(MemberVersion2 member) {
        return convertMemberToVersion3WithGroupId(member, member.getGroupId());
    }

    protected Member convertMemberToVersion3WithGroupId(MemberVersion2 member, long groupId) {

        Log.d(TAG, "convertMemberToVersion3WithGroupId " + member.getName() + " old groupId: " + member.getGroupId() + " new groupId:" + groupId + " contact: " + member.getContactMode());

        // we also need a group because of the constraint
        Group.GroupBuilder groupBuilder = new Group.GroupBuilder();
        Member.MemberBuilder memberBuilder = new Member.MemberBuilder();
        ContactMethod convertedContactMethod;

        if (member.getContactMode() == ConstantsVersion2.EMAIL_CONTACT_MODE) {
            convertedContactMethod = ContactMethod.EMAIL;
        } else if (member.getContactMode() == ConstantsVersion2.SMS_CONTACT_MODE) {
            convertedContactMethod = ContactMethod.SMS;
        } else {
            convertedContactMethod = ContactMethod.REVEAL_ONLY;
        }

        Member convertedMember = memberBuilder.withName(member.getName())
                .withContactMethod(convertedContactMethod)
                .withContactDetails(member.getContactDetail())
                .withGroup(groupBuilder.withGroupId(groupId).build())
                .withLookupKey(member.getLookupKey())
                .build();

        return convertedMember;
    }

    // The trickiest one is building the Assignment table
    // i) for each group, get all the Draw Results
    //    delete that group entry
    //    and with each Draw Result, add a new entry as a new Group (name is group + draw date)
    //    so each old Draw Results will now be a new Group (new schema rules)
    //    - migrate the message from Draw Result to the Group
    //    - migrate the draw date from Draw Result to the Group
    //    - set Group created date = draw date
    protected void migrateDataToVersion3AssignmentsTable(SQLiteDatabase db) {
        Log.d(TAG, "migrateDataToVersion3AssignmentsTable");
        List<GroupVersion2> groupsVersion2 = mDbHelper.queryAll(GroupVersion2.class);

        for (GroupVersion2 groupVersion2 : groupsVersion2) {

            boolean transactionSuccess = true;
            // migrate each group in a transaction so that we can rollback on a per group basis in case of errors
            // and not have an all or nothing migration
            //db.beginTransaction();
            try {
                List<DrawResultVersion2> drawResultsVersion2 = getAllDrawResultsVersion2ForGroup(groupVersion2.getId());

                // if the group was not ready, we want to migrate it as is
                if (!groupVersion2.isReady()) {
                    insertMigratedGroupInfo(groupVersion2);
                }

                // we want to also migrate the draw results
                int drawResult = 1; // keep track of how many draw results
                for (DrawResultVersion2 drawResultVersion2 : drawResultsVersion2) {
                    long groupId;
                    Log.d(TAG, "group: " + groupVersion2.getName() + " draw: " + drawResult);
                    // with the new schema, we need to convert all draw results into groups
                    // so if there is only one group and one draw result, we just update the group info.
                    // otherwise if the group has more than one draw result or was in the not ready state
                    // (which means that it has currently been migrated in its built, not ready state)
                    // we create a second group for that and so on
                    if (drawResult == 1 && groupVersion2.isReady()) {
                        groupId = groupVersion2.getId();
                        //update the group
                        updateMigratedGroupInfo(drawResultVersion2, groupVersion2.getName(), drawResult);
                    }   else {
                        // we are up to the second or more draw result or the group was not ready
                        // we need to add a new group to capture that draw info
                        groupId = addMigratedGroupInfo(drawResultVersion2, groupVersion2.getName(), drawResult);
                    }

                    drawResult++;
                    // for each Draw Result Entry, build the entry for the new Assignment
                    //    Assignment columns are
                    //    - giver member id and receiver member id
                    //     (you need to get the member id from the given member name in the Member table)
                    //    - send status
                    //     (work it out from the Draw Result Entry sent and/or viewed date, if not set to assigned)
                    //

                    try {

                        // get all the draw result entries corresponding to this draw result entry
                        List<DrawResultEntryVersion2> drawResultEntriesVersion2 = mDbHelper.getDaoEx(DrawResultEntryVersion2.class).queryBuilder()
                                .where().eq(DrawResultEntryVersion2.Columns.DRAW_RESULT_ID_COLUMN, drawResultVersion2.getId())
                                .query();

                        for (DrawResultEntryVersion2 drawResultEntryVersion2 : drawResultEntriesVersion2) {
                            insertMigratedAssignmentEntryInfo(drawResultEntryVersion2, groupId);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Exception in migrateDataToVersion3AssignmentsTable going through the DrawResultEntries. Rolling back migration for groupId:" + groupVersion2.getId() + ". Exception: " + e.getMessage());
                        // set flag to false so that the transaction rollsback
                        transactionSuccess = false;
                        break; // out of the draw results loop, no point continuing
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception in migrateDataToVersion3AssignmentsTable. Rolling back migration for groupId:" + groupVersion2.getId() + ". Exception: " + e.getMessage());
                // set flag to false so that the transaction rollsback
                transactionSuccess = false;
            }

//            if (transactionSuccess) {
//                //let's commit it
//                db.setTransactionSuccessful();
//            }
//
//            db.endTransaction();
        }

        Log.d(TAG, "migrateDataToVersion3AssignmentsTable reached end of method");
    }


    protected void updateMigratedGroupInfo(DrawResultVersion2 drawResultVersion2, String version2GroupName, int drawResultNumber) {

        Group.GroupBuilder groupBuilder = new Group.GroupBuilder();

        // we set created date = draw date
        Group migratedGroup = groupBuilder
                .withGroupId(drawResultVersion2.getGroupId())
                .withName(constructNewMigratedGroupName(version2GroupName + " #" + drawResultNumber))
                .withMessage(drawResultVersion2.getMessage())
                .withDrawDate(drawResultVersion2.getDrawDate())
                .withCreatedDate(drawResultVersion2.getDrawDate())
                .build();

        mDbHelper.update(migratedGroup, Group.class);
    }

    protected long addMigratedGroupInfo(DrawResultVersion2 drawResultVersion2, String version2GroupName, int drawResultNumber) {

        Log.d(TAG, "addMigratedGroupInfo " + drawResultVersion2.getGroupId() + " " + version2GroupName + " drawResult:" + drawResultNumber);
        Group.GroupBuilder groupBuilder = new Group.GroupBuilder();

        // we set created date = draw date
        // and add the draw number to the group name
        Group migratedGroup = groupBuilder
                .withName(constructNewMigratedGroupName(version2GroupName + " #" + drawResultNumber))
                .withMessage(drawResultVersion2.getMessage())
                .withDrawDate(drawResultVersion2.getDrawDate())
                .withCreatedDate(drawResultVersion2.getDrawDate())
                .build();

        long newGroupId = mDbHelper.create(migratedGroup, Group.class);
        duplicateAllMembers(drawResultVersion2.getGroupId(), newGroupId);

        return newGroupId;
    }

    protected void duplicateAllMembers(long oldGroupId, long newGroupId) {
        try {
            Log.d(TAG, "duplicateAllMembers from oldGroup: " + oldGroupId + "to newGroup:" + newGroupId);

            // we need to duplicate all members (with the new group id)
            // copy all data from old Member to new Member
            List<MemberVersion2> allMembersVersion2 = mDbHelper.getDaoEx(MemberVersion2.class).queryBuilder()
                                                                            .where().eq(MemberVersion2.Columns.GROUP_ID_COLUMN, oldGroupId)
                                                                            .query();
            for (MemberVersion2 memberVersion2 : allMembersVersion2) {
                Member duplicatedMember = convertMemberToVersion3WithGroupId(memberVersion2, newGroupId);
                Log.d(TAG, "duplicate member to insert:" + duplicatedMember.getName() +
                        " method: " + duplicatedMember.getContactMethod() +
                        " group: " + duplicatedMember.getGroupId());

                mDbHelper.create(duplicatedMember, Member.class);
            }

        } catch (SQLException e) {
            throw new android.database.SQLException(e.getMessage());
        }
    }




    public static String constructNewMigratedGroupName(String groupName) {
        return groupName + " (auto-migrated)";
    }

    protected void insertMigratedGroupInfo(GroupVersion2 groupVersion2) {

        Group.GroupBuilder groupBuilder = new Group.GroupBuilder();
        // we set created date = draw date
        Group migratedGroup = groupBuilder
                    .withGroupId(groupVersion2.getId())
                    .withName(constructNewMigratedGroupName(groupVersion2.getName()))
                    .withCreatedDate(System.currentTimeMillis())
                    .build();

        mDbHelper.update(migratedGroup, Group.class);
    }

    protected void insertMigratedAssignmentEntryInfo(DrawResultEntryVersion2 drawResultEntryVersion2, long groupId) {
        Log.d(TAG, "insertMigratedAssignmentEntryInfo groupId " + groupId);
        long giverMemberId = getMemberIdFromMemberName(drawResultEntryVersion2.getGiverName(), groupId);
        long receiverMemberId = getMemberIdFromMemberName(drawResultEntryVersion2.getReceiverName(), groupId);

        Assignment newAssignment = new Assignment();

        // foreign key being Member, we need to query for the Member object to set it
        Member giverMember = mDbHelper.queryById(giverMemberId, Member.class);
        newAssignment.setGiverMember(giverMember);

        Member receiverMember = mDbHelper.queryById(receiverMemberId, Member.class);
        newAssignment.setReceiverMember(receiverMember);

        Assignment.Status sendStatus = Assignment.Status.Assigned; // default
        if (drawResultEntryVersion2.getViewedDate() > 0) {
            sendStatus = Assignment.Status.Revealed;
        }

        // we show the latest status, so we need to compare revealed and sent date
        if (drawResultEntryVersion2.getSentDate() > 0 && drawResultEntryVersion2.getSentDate() > drawResultEntryVersion2.getViewedDate()) {
            sendStatus = Assignment.Status.Sent;
        }
        newAssignment.setSendStatus(sendStatus);

        mDbHelper.create(newAssignment, Assignment.class);
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
            SelectArg selectArg = new SelectArg();
            selectArg.setValue(memberName);

            Member member = mDbHelper.getDaoEx(Member.class).queryBuilder()
                                       .where().eq(Member.Columns.NAME_COLUMN, selectArg).and()
                                               .eq(Member.Columns.GROUP_ID_COLUMN, groupId)
                                       .queryForFirst();
            if (member != null) {
                return member.getId();
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

        // we need the Group object to set it in the Member, so let's query the db
        Group group = mDbHelper.queryById(groupId, Group.class);
        newMember.setGroup(group);
        return mDbHelper.create(newMember, Member.class);
    }
}
