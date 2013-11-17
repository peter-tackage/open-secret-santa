package com.moac.android.opensecretsanta.database;

import android.test.AndroidTestCase;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.ContactMethod;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.version2.*;

import java.util.List;


public class DatabaseUpgraderVersion3TablesTests extends AndroidTestCase {

    private static final String TEST_DATABASE_NAME = "testopensecretsanta.db";

    TestDatabaseHelper mTestDbHelper;
    DatabaseUpgrader mDatabaseUpgrader;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        getContext().deleteDatabase("/data/data/com.moac.android.opensecretsanta/databases/" + TEST_DATABASE_NAME);
        mDatabaseUpgrader = null;
    }

    private void getVersion3Tables() {
        Class[] PERSISTABLE_OBJECTS = new Class[]
                { Member.class, Group.class };

        mTestDbHelper = new TestDatabaseHelper(getContext(), TEST_DATABASE_NAME, PERSISTABLE_OBJECTS);
        mTestDbHelper.getWritableDatabase().beginTransaction();

        mDatabaseUpgrader = new DatabaseUpgrader(mTestDbHelper);
    }

    private void getVersion2Tables() {
        Class[] PERSISTABLE_OBJECTS = new Class[]
                { GroupVersion2.class, MemberVersion2.class, DrawResultVersion2.class, DrawResultEntryVersion2.class, RestrictionVersion2.class};

        mTestDbHelper = new TestDatabaseHelper(getContext(), TEST_DATABASE_NAME, PERSISTABLE_OBJECTS);
        mTestDbHelper.getWritableDatabase().beginTransaction();

        mDatabaseUpgrader = new DatabaseUpgrader(mTestDbHelper);

        // we need to run the alter queries so as to get the new table version3 setup for these tests
        mDatabaseUpgrader.alterMemberTable();
        mDatabaseUpgrader.alterGroupTable();
    }

    // so that we can test migration of assignment
    private void getVersion2TablesAndAssignment() {
        Class[] PERSISTABLE_OBJECTS = new Class[]
                { Assignment.class, GroupVersion2.class, MemberVersion2.class, DrawResultVersion2.class, DrawResultEntryVersion2.class};

        mTestDbHelper = new TestDatabaseHelper(getContext(), TEST_DATABASE_NAME, PERSISTABLE_OBJECTS);
        mTestDbHelper.getWritableDatabase().beginTransaction();

        mDatabaseUpgrader = new DatabaseUpgrader(mTestDbHelper);

        // we need to run the alter queries so as to get the new table version3 setup for these tests
        mDatabaseUpgrader.alterMemberTable();
        mDatabaseUpgrader.alterGroupTable();
    }

    public void testInsertMigratedAssignmentEntryInfoAssigned () {

        getVersion2TablesAndAssignment();
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

        mDatabaseUpgrader.insertMigratedAssignmentEntryInfo(drawResultEntry, expectedGroupId);

        List<Assignment> assignmentsTestResult = mTestDbHelper.queryAll(Assignment.class);

        assertTrue(assignmentsTestResult.size() == 1);

        Assignment assignmentTestResult = assignmentsTestResult.get(0);
        assertEquals(expectedGiverMemberId, assignmentTestResult.getGiverMemberId());
        assertEquals(expectedReceiverMemberId, assignmentTestResult.getReceiverMemberId());
        assertEquals(Assignment.Status.Assigned, assignmentTestResult.getSendStatus());
    }

    public void testInsertMigratedAssignmentEntryInfoRevealed () {

        getVersion2TablesAndAssignment();
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

    public void testInsertMigratedGroupInfo() {
        getVersion2TablesAndAssignment();
        String expectedGroupName = "migrated groupName 1234";
        String groupName = "groupName";
        String expectedTestMessage = "Hello there test message";
        long expectedDrawDate = 1234;

        Group expectedGroup = new Group();
        expectedGroup.setName(expectedGroupName);
        expectedGroup.setDrawDate(expectedDrawDate);
        expectedGroup.setMessage(expectedTestMessage);
        expectedGroup.setCreatedAt(expectedDrawDate);

        DrawResultVersion2 drawResultVersion2 = new DrawResultVersion2();
        drawResultVersion2.setDrawDate(expectedDrawDate);
        drawResultVersion2.setMessage(expectedTestMessage);

        mDatabaseUpgrader.insertMigratedGroupInfo(drawResultVersion2, groupName);

        // query if expected group exists indeed
        List<Group> testResultGroups = mTestDbHelper.queryAll(Group.class);

        assertEquals(1, testResultGroups.size());
        // there should only be the one and only, let's grab it
        Group oneAndOnlyTestResultGroup = testResultGroups.get(0);
        assertEquals(expectedGroup, oneAndOnlyTestResultGroup);
    }

    public void testGetMemberIdFromMemberNameExistingMember() {
        getVersion3Tables();
        // we need to have a group created as there is a constraint between the group and the member
        String groupName = "groupName";
        Group group = new Group();
        group.setName(groupName);
        long groupId =  mTestDbHelper.create(group, Group.class);

        String testMemberName = "memberName";
        Member expectedMember = new Member();
        expectedMember.setName(testMemberName);
        expectedMember.setGroup(group);
        expectedMember.setContactMethod(ContactMethod.EMAIL);
        expectedMember.setContactAddress("member@me.com");
        expectedMember.setLookupKey("lookupkey");
        mTestDbHelper.create(expectedMember, Member.class);

        mDatabaseUpgrader.getMemberIdFromMemberName(testMemberName, groupId);
        // check that the member have been created
        List<Member> membersTestResult = mTestDbHelper.queryAll(Member.class);
        assertEquals(1, membersTestResult.size());
        // we can assume that the entries will be in the same order i.e giver Member is first element
        assertEquals(expectedMember, membersTestResult.get(0));
    }

     public void testGetMemberIdFromMemberNameInexistentMember() {
        getVersion3Tables();
        // we need to have a group created as there is a constraint between the group and the member
        String groupName = "groupName";
        Group group = new Group();
        group.setName(groupName);
        long groupId =  mTestDbHelper.create(group, Group.class);

        String newMemberName = "newMemberName";

        Member expectedMember = new Member();
        expectedMember.setName(newMemberName);
        expectedMember.setContactMethod(ContactMethod.REVEAL_ONLY);
        expectedMember.setGroup(group);

        mDatabaseUpgrader.getMemberIdFromMemberName(newMemberName, groupId);
        // check that the member have been created
        List<Member> membersTestResult = mTestDbHelper.queryAll(Member.class);
        assertEquals(1, membersTestResult.size());
        // we can assume that the entries will be in the same order i.e giver Member is first element
        assertEquals(expectedMember, membersTestResult.get(0));
    }

//    public void testMigrateDataToVersion3AssignmentsTable() {
//
//        getVersion2Tables();
//        // create an old db
//        // big migration
//
//
//        // query new tables to check results
//    }
}
