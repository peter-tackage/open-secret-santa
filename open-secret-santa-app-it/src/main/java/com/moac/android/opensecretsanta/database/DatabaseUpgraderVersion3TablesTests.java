package com.moac.android.opensecretsanta.database;

import android.test.AndroidTestCase;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.version2.DrawResultEntryVersion2;
import com.moac.android.opensecretsanta.model.version2.DrawResultVersion2;
import com.moac.android.opensecretsanta.model.version2.GroupVersion2;
import com.moac.android.opensecretsanta.model.version2.MemberVersion2;

import java.util.List;


public class DatabaseUpgraderVersion3TablesTests extends AndroidTestCase {

    private static final String TEST_DATABASE_NAME = "testopensecretsanta.db";

    TestDatabaseHelper mTestDbHelper;
    DatabaseUpgrader mDatabaseUpgrader;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Class[] PERSISTABLE_OBJECTS = new Class[]
                { Assignment.class, GroupVersion2.class, MemberVersion2.class, DrawResultVersion2.class, DrawResultEntryVersion2.class};

        mTestDbHelper = new TestDatabaseHelper(getContext(), TEST_DATABASE_NAME, PERSISTABLE_OBJECTS);
        mTestDbHelper.getWritableDatabase().beginTransaction();

        mDatabaseUpgrader = new DatabaseUpgrader(mTestDbHelper);

        // we need to run the alter queries so as to get the new table version3 setup for these tests
        mDatabaseUpgrader.alterMemberTable();
        mDatabaseUpgrader.alterGroupTable();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mTestDbHelper.dropAllTables();
        mTestDbHelper.getWritableDatabase().endTransaction();
        mDatabaseUpgrader = null;
    }

    public void testInsertMigratedAssignmentEntryInfoAssigned () {

        String expectedGiverName = "giver";
        String expectedReceiverName = "receiver";
        // We need a group containing those two members giver and receiver
        // and the draw result that contains the draw result entry needs to reference that group id
        String expectedGroupName = "testGroup";
        GroupVersion2 expectedGroup = new GroupVersion2();
        expectedGroup.setName(expectedGroupName);
        long expectedGroupId = mTestDbHelper.create(expectedGroup, GroupVersion2.class);

        DrawResultVersion2 expectedDrawResult = new DrawResultVersion2();
        expectedDrawResult.setGroup(expectedGroup);
        long expectedDrawResultId = mTestDbHelper.create(expectedDrawResult, DrawResultVersion2.class);

        MemberVersion2 giver = new MemberVersion2();
        giver.setName(expectedGiverName);
        giver.setGroup(expectedGroup);
        long expectedGiverMemberId = mTestDbHelper.create(giver, MemberVersion2.class);

        MemberVersion2 receiver = new MemberVersion2();
        receiver.setName(expectedReceiverName);
        receiver.setGroup(expectedGroup);
        long expectedReceiverMemberId = mTestDbHelper.create(receiver, MemberVersion2.class);

        DrawResultEntryVersion2 drawResultEntry = new DrawResultEntryVersion2();
        drawResultEntry.setGiverName(expectedGiverName);
        drawResultEntry.setReceiverName(expectedReceiverName);

        // only assigned, no sent date set

        mTestDbHelper.create(drawResultEntry, DrawResultEntryVersion2.class);

        mDatabaseUpgrader.insertMigratedAssignmentEntryInfo(drawResultEntry, expectedGroupId);

        List<Assignment> assignmentsTestResult = mTestDbHelper.queryAll(Assignment.class);

        assertTrue(assignmentsTestResult.size() == 1);

        Assignment assignmentTestResult = assignmentsTestResult.get(0);
        assertEquals(expectedGiverMemberId, assignmentTestResult.getGiverMemberId());
        assertEquals(expectedReceiverMemberId, assignmentTestResult.getReceiverMemberId());
        assertEquals(Assignment.Status.Assigned, assignmentTestResult.getSendStatus());
    }

    public void testInsertMigratedAssignmentEntryInfoRevealed () {

        String expectedGiverName = "giver";
        String expectedReceiverName = "receiver";
        // We need a group containing those two members giver and receiver
        // and the draw result that contains the draw result entry needs to reference that group id
        String expectedGroupName = "testGroup";
        GroupVersion2 expectedGroup = new GroupVersion2();
        expectedGroup.setName(expectedGroupName);
        long expectedGroupId = mTestDbHelper.create(expectedGroup, GroupVersion2.class);

        DrawResultVersion2 expectedDrawResult = new DrawResultVersion2();
        expectedDrawResult.setGroup(expectedGroup);
        long expectedDrawResultId = mTestDbHelper.create(expectedDrawResult, DrawResultVersion2.class);

        MemberVersion2 giver = new MemberVersion2();
        giver.setName(expectedGiverName);
        giver.setGroup(expectedGroup);
        long expectedGiverMemberId = mTestDbHelper.create(giver, MemberVersion2.class);

        MemberVersion2 receiver = new MemberVersion2();
        receiver.setName(expectedReceiverName);
        receiver.setGroup(expectedGroup);
        long expectedReceiverMemberId = mTestDbHelper.create(receiver, MemberVersion2.class);

        DrawResultEntryVersion2 drawResultEntry = new DrawResultEntryVersion2();
        drawResultEntry.setGiverName(expectedGiverName);
        drawResultEntry.setReceiverName(expectedReceiverName);

        // draw result has been viewed
        drawResultEntry.setSentDate(System.currentTimeMillis());
        drawResultEntry.setViewedDate(System.currentTimeMillis());
        mTestDbHelper.create(drawResultEntry, DrawResultEntryVersion2.class);

        mDatabaseUpgrader.insertMigratedAssignmentEntryInfo(drawResultEntry, expectedGroupId);

        List<Assignment> assignmentsTestResult = mTestDbHelper.queryAll(Assignment.class);

        assertTrue(assignmentsTestResult.size() == 1);

        Assignment assignmentTestResult = assignmentsTestResult.get(0);
        assertEquals(expectedGiverMemberId, assignmentTestResult.getGiverMemberId());
        assertEquals(expectedReceiverMemberId, assignmentTestResult.getReceiverMemberId());
        assertEquals(Assignment.Status.Revealed, assignmentTestResult.getSendStatus());
    }



    // Can't really test this as we need both old group and new group table class... hmm
//    public void testInsertMigratedGroupInfo() {
//        String expectedGroupName = "groupName";
//        String expectedTestMessage = "Hello there test message";
//        long expectedDrawDate = 1234;
//        DrawResultVersion2 drawResultVersion2 = new DrawResultVersion2();
//        drawResultVersion2.setDrawDate(expectedDrawDate);
//        drawResultVersion2.setMessage(expectedTestMessage);
//
//        mDatabaseUpgrader.insertMigratedGroupInfo(drawResultVersion2, expectedGroupName);
//
//        // query if expected group exists indeed
//      //  Group testResultGroup = mTestDbHelper.queryAll(Group.class);
//
//    }

    // Can't really test this as we need both old member and new member table class... hmm
//    public void testCreateMemberIfInexistent() {
//        // we need to have a group created as there is a constraint between the group and the member
//        String groupName = "groupName";
//        GroupVersion2 group = new GroupVersion2();
//        group.setName(groupName);
//        long groupId =  mTestDbHelper.create(group, GroupVersion2.class);
//
//        String newMemberName = "newMemberName";
//        mDatabaseUpgrader.getMemberIdFromMemberName(newMemberName, groupId);
//        // check that the members have been created
//        List<Member> membersTestResult = mTestDbHelper.queryAll(Member.class);
//        assertEquals(1, membersTestResult.size());
//        // we can assume that the entries will be in the same order i.e giver Member is first element
//        assertEquals(newMemberName, membersTestResult.get(0).getName());
//    }
}
