package com.moac.android.opensecretsanta.database;

import android.test.AndroidTestCase;
import com.moac.android.opensecretsanta.builders.DrawResultEntryVersion2Builder;
import com.moac.android.opensecretsanta.builders.DrawResultVersion2Builder;
import com.moac.android.opensecretsanta.builders.GroupVersion2Builder;
import com.moac.android.opensecretsanta.builders.MemberVersion2Builder;
import com.moac.android.opensecretsanta.model.*;
import com.moac.android.opensecretsanta.model.version2.DrawResultEntryVersion2;
import com.moac.android.opensecretsanta.model.version2.DrawResultVersion2;
import com.moac.android.opensecretsanta.model.version2.GroupVersion2;
import com.moac.android.opensecretsanta.model.version2.MemberVersion2;

import java.util.List;


// NOTE that id returned by create to old tables should not be used to query the new tables!
// That would not make sense, although one exception is the Restriction table which hasn't changed
public class DatabaseUpgraderVersion3TablesTests extends AndroidTestCase {

    private static final String TEST_DATABASE_NAME = "testopensecretsanta.db";
    private static final String GROUP_MIGRATED = " (auto-migrated)";
    TestDatabaseHelper mTestDbHelper;
    DatabaseUpgrader mDatabaseUpgrader;


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        getContext().deleteDatabase("/data/data/com.moac.android.opensecretsanta/databases/" + TEST_DATABASE_NAME);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        if (mTestDbHelper != null) {
            mTestDbHelper.getWritableDatabase().endTransaction();
        }
        mDatabaseUpgrader = null;
    }

    // put some helper equality method here to be used in tests
    private boolean isGroupVersion2Equal(GroupVersion2 a, GroupVersion2 b) {
        if(a == null || b == null) {
            return false;
        }
        return ((a.getName().compareTo(b.getName()) == 0) &&
                a.isReady() == b.isReady());
    }

    private boolean isDrawResultVersion2Equal(DrawResultVersion2 a, DrawResultVersion2 b) {
        if(a == null || b == null) {
            return false;
        }
        return (a.getDrawDate() == (b.getDrawDate()) &&
                a.getSendDate() == (b.getSendDate()) &&
                (a.getMessage().compareTo(b.getMessage()) == 0)) &&
                (a.getGroupId() == (b.getGroupId()));
    }

    private boolean isAssignmentEqual(Assignment a, Assignment b) {
        if(a == null || b == null) {
            return false;
        }
        return (a.getGiverMemberId() == (b.getGiverMemberId()) &&
                a.getReceiverMemberId() == (b.getReceiverMemberId()) &&
                a.getSendStatus().equals(b.getSendStatus()));
    }

    private boolean isGroupEqual(Group a, Group b) {
        if(a == null || b == null) {
            return false;
        }
        // no need to check created at date as that's automatic on object creation which will differ
        return (a.getDrawDate() == (b.getDrawDate()) &&
                (a.getMessage().compareTo(b.getMessage()) == 0) &&
                (a.getName().compareTo(b.getName()) == 0));
    }

    private boolean isMemberEqual(Member a, Member b) {
        if(a == null || b == null) {
            return false;
        }

        return (a.getContactId() == b.getContactId() &&
                (a.getLookupKey() == null? b.getLookupKey() == null : a.getLookupKey().compareTo(b.getLookupKey()) == 0)  &&
                (a.getContactDetails() == null ?  b.getContactDetails() == null : a.getContactDetails().compareTo(b.getContactDetails()) == 0) &&
                a.getGroupId() == b.getGroupId() &&
                a.getContactMethod() == b.getContactMethod() &&
                (a.getName().compareTo(b.getName()) == 0));
    }

    private boolean isMemberInsideList(Member a, List<Member> memberList) {
        for (int i=0; i<memberList.size(); i++) {
            if (isMemberEqual(a, memberList.get(i))) {
                return true;
            }
        }

        return false;
    }

    private boolean isAssignmentInsideList(Assignment a, List<Assignment> assignmentList) {
        for (int i=0; i<assignmentList.size(); i++) {
            if (isAssignmentEqual(a, assignmentList.get(i))) {
                return true;
            }
        }

        return false;
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
                { Member.class, GroupVersion2.class, MemberVersion2.class, DrawResultVersion2.class, DrawResultEntryVersion2.class, Restriction.class};

        mTestDbHelper = new TestDatabaseHelper(getContext(), TEST_DATABASE_NAME, PERSISTABLE_OBJECTS);
        mTestDbHelper.getWritableDatabase().beginTransaction();

        mDatabaseUpgrader = new DatabaseUpgrader(mTestDbHelper);

        // we need to run the alter queries so as to get the new table version3 setup for these tests
        mDatabaseUpgrader.addNewAssignmentTable(mTestDbHelper.mConnectionSource);
        mDatabaseUpgrader.alterGroupTable();
    }

    // so that we can test migration of assignment
    private void getVersion2TablesAndAssignment() {
        Class[] PERSISTABLE_OBJECTS = new Class[]
                { Member.class, Assignment.class, GroupVersion2.class, MemberVersion2.class, DrawResultVersion2.class, DrawResultEntryVersion2.class};

        mTestDbHelper = new TestDatabaseHelper(getContext(), TEST_DATABASE_NAME, PERSISTABLE_OBJECTS);
        mTestDbHelper.getWritableDatabase().beginTransaction();

        mDatabaseUpgrader = new DatabaseUpgrader(mTestDbHelper);
        mDatabaseUpgrader.alterGroupTable();
    }


    public void testMigrateMemberTable() {

        Class[] PERSISTABLE_OBJECTS = new Class[]
                { Restriction.class, MemberVersion2.class, GroupVersion2.class };

        mTestDbHelper = new TestDatabaseHelper(getContext(), TEST_DATABASE_NAME, PERSISTABLE_OBJECTS);
        mTestDbHelper.getWritableDatabase().beginTransaction();

        mDatabaseUpgrader = new DatabaseUpgrader(mTestDbHelper);

        GroupVersion2Builder groupVersion2Builder = new GroupVersion2Builder();
        GroupVersion2 groupVersion2 = groupVersion2Builder.withName("test").build();
        long groupId = mTestDbHelper.create(groupVersion2, GroupVersion2.class);

        // contact mode : reveal
        MemberVersion2Builder memberVersion2Builder = new MemberVersion2Builder();
        MemberVersion2 memberVersion2One = memberVersion2Builder.withName("test")
        .withContactMode(0).withGroup(groupVersion2).build();
        mTestDbHelper.create(memberVersion2One, MemberVersion2.class);

        // sms
        MemberVersion2Builder memberVersion2BuilderTwo = new MemberVersion2Builder();
        MemberVersion2 memberVersion2Two = memberVersion2BuilderTwo.withName("test2")
                .withContactMode(1).withGroup(groupVersion2).build();
        mTestDbHelper.create(memberVersion2Two, MemberVersion2.class);

        // email
        MemberVersion2Builder memberVersion2BuilderThree = new MemberVersion2Builder();
        MemberVersion2 memberVersion2Three = memberVersion2BuilderThree.withName("test3")
                .withContactMode(2).withGroup(groupVersion2).build();
        mTestDbHelper.create(memberVersion2Three, MemberVersion2.class);


        // call only the relevant migration methods

        mDatabaseUpgrader.createNewMemberVersion3Table(mTestDbHelper.mConnectionSource);
        mDatabaseUpgrader.migrateMemberAndRestrictionsTable(mTestDbHelper.getConnectionSource());
        mDatabaseUpgrader.alterGroupTable();

        Group migratedGroup = mTestDbHelper.queryById(groupId, Group.class);

        Member.MemberBuilder memberBuilder = new Member.MemberBuilder();
        Member expectedMigratedMemberOne = memberBuilder.withName(memberVersion2One.getName())
                .withLookupKey(memberVersion2One.getLookupKey())
                .withContactMethod(ContactMethod.REVEAL_ONLY)
                .withContactDetails(memberVersion2One.getContactDetail())
                .withGroup(migratedGroup).build();

        Member.MemberBuilder memberBuilderTwo = new Member.MemberBuilder();
        Member expectedMigratedMemberTwo = memberBuilderTwo.withName(memberVersion2Two.getName())
                .withLookupKey(memberVersion2Two.getLookupKey())
                .withContactMethod(ContactMethod.SMS)
                .withContactDetails(memberVersion2Two.getContactDetail())
                .withGroup(migratedGroup).build();

        Member.MemberBuilder memberBuilderThree = new Member.MemberBuilder();
        Member expectedMigratedMemberThree = memberBuilderThree.withName(memberVersion2Three.getName())
                .withLookupKey(memberVersion2Three.getLookupKey())
                .withContactMethod(ContactMethod.EMAIL)
                .withContactDetails(memberVersion2Three.getContactDetail())
                .withGroup(migratedGroup).build();

        List<Member> migratedMembers = mTestDbHelper.queryAll(Member.class);
        assertEquals(3, migratedMembers.size());
        assertTrue(isMemberEqual(expectedMigratedMemberOne, migratedMembers.get(0)));
        assertTrue(isMemberEqual(expectedMigratedMemberTwo, migratedMembers.get(1)));
        assertTrue(isMemberEqual(expectedMigratedMemberThree, migratedMembers.get(2)));
    }

    public void testInsertMigratedAssignmentEntryInfoAssigned () {
        // check this test
        getVersion2TablesAndAssignment();

        String memberAName = "memberA";
        String memberBName = "memberB";
        // We need a group containing those two members giver and receiver
        // and the draw result that contains the draw result entry needs to reference that group id
        String expectedGroupName = "testGroup";
        GroupVersion2 expectedGroup = new GroupVersion2();
        expectedGroup.setName(expectedGroupName);
        long expectedGroupId = mTestDbHelper.create(expectedGroup, GroupVersion2.class);

        DrawResultVersion2 expectedDrawResult = new DrawResultVersion2();
        expectedDrawResult.setGroup(expectedGroup);
        mTestDbHelper.create(expectedDrawResult, DrawResultVersion2.class);

        MemberVersion2 memberA = new MemberVersion2();
        memberA.setName(memberAName);
        memberA.setGroup(expectedGroup);
        memberA.setContactMode(0);
        long expectedMemberAId = mTestDbHelper.create(memberA, MemberVersion2.class);

        MemberVersion2 memberB = new MemberVersion2();
        memberB.setName(memberBName);
        memberB.setGroup(expectedGroup);
        memberB.setContactMode(0);
        long expectedMemberBId = mTestDbHelper.create(memberB, MemberVersion2.class);

        // only assigned, no sent date set
        DrawResultEntryVersion2 drawResultEntry = new DrawResultEntryVersion2();
        drawResultEntry.setGiverName(memberAName);
        drawResultEntry.setReceiverName(memberBName);

        mDatabaseUpgrader.insertMigratedAssignmentEntryInfo(drawResultEntry, expectedGroupId);

        List<Assignment> assignmentsTestResult = mTestDbHelper.queryAll(Assignment.class);

        assertTrue(assignmentsTestResult.size() == 1);

        // A -> B
        Assignment assignmentTestResultAB = assignmentsTestResult.get(0);
        assertEquals(expectedMemberAId, assignmentTestResultAB.getGiverMemberId());
        assertEquals(expectedMemberBId, assignmentTestResultAB.getReceiverMemberId());
        assertEquals(Assignment.Status.Assigned, assignmentTestResultAB.getSendStatus());
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
        mTestDbHelper.create(expectedDrawResult, DrawResultVersion2.class);

        MemberVersion2 giver = new MemberVersion2();
        giver.setName(expectedGiverName);
        giver.setGroup(expectedGroup);
        giver.setContactMode(0);
        long expectedGiverMemberId = mTestDbHelper.create(giver, MemberVersion2.class);

        MemberVersion2 receiver = new MemberVersion2();
        receiver.setName(expectedReceiverName);
        receiver.setGroup(expectedGroup);
        receiver.setContactMode(0);
        long expectedReceiverMemberId = mTestDbHelper.create(receiver, MemberVersion2.class);

        DrawResultEntryVersion2 drawResultEntry = new DrawResultEntryVersion2();
        drawResultEntry.setGiverName(expectedGiverName);
        drawResultEntry.setReceiverName(expectedReceiverName);

        // draw result has been viewed , sent and viewed
        drawResultEntry.setViewedDate(System.currentTimeMillis());
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

    public void testInsertMigratedAssignmentEntryInfoSent () {

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
        mTestDbHelper.create(expectedDrawResult, DrawResultVersion2.class);

        MemberVersion2 giver = new MemberVersion2();
        giver.setName(expectedGiverName);
        giver.setGroup(expectedGroup);
        giver.setContactMode(1);
        long expectedGiverMemberId = mTestDbHelper.create(giver, MemberVersion2.class);

        MemberVersion2 receiver = new MemberVersion2();
        receiver.setName(expectedReceiverName);
        receiver.setGroup(expectedGroup);
        receiver.setContactMode(1);
        long expectedReceiverMemberId = mTestDbHelper.create(receiver, MemberVersion2.class);

        DrawResultEntryVersion2 drawResultEntry = new DrawResultEntryVersion2();
        drawResultEntry.setGiverName(expectedGiverName);
        drawResultEntry.setReceiverName(expectedReceiverName);

        // draw result has been sent but not viewed
        drawResultEntry.setSentDate(System.currentTimeMillis());
        mTestDbHelper.create(drawResultEntry, DrawResultEntryVersion2.class);

        mDatabaseUpgrader.insertMigratedAssignmentEntryInfo(drawResultEntry, expectedGroupId);

        List<Assignment> assignmentsTestResult = mTestDbHelper.queryAll(Assignment.class);

        assertTrue(assignmentsTestResult.size() == 1);

        Assignment assignmentTestResult = assignmentsTestResult.get(0);
        assertEquals(expectedGiverMemberId, assignmentTestResult.getGiverMemberId());
        assertEquals(expectedReceiverMemberId, assignmentTestResult.getReceiverMemberId());
        assertEquals(Assignment.Status.Sent, assignmentTestResult.getSendStatus());
    }


    public void testUpdateMigratedGroupInfo() {

        // shortcut set up the tables
        getVersion2TablesAndAssignment();

        // we need to insert the group to be migrated first
        GroupVersion2Builder groupVersion2Builder = new GroupVersion2Builder();

        String groupName = "groupName";
        GroupVersion2 groupVersion2 = groupVersion2Builder.withName(groupName).build();
        mTestDbHelper.create(groupVersion2, GroupVersion2.class);

        String expectedGroupName = groupName + " #1" + GROUP_MIGRATED;
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
        drawResultVersion2.setGroup(groupVersion2);

        mDatabaseUpgrader.updateMigratedGroupInfo(drawResultVersion2, groupName, 1);

        // query if expected group exists indeed
        List<Group> testResultGroups = mTestDbHelper.queryAll(Group.class);

        assertEquals(1, testResultGroups.size());
        // there should only be the one and only, let's grab it
        Group oneAndOnlyTestResultGroup = testResultGroups.get(0);
        assertTrue(isGroupEqual(expectedGroup, oneAndOnlyTestResultGroup));
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
        expectedMember.setContactDetails("member@me.com");
        expectedMember.setLookupKey("lookupkey");
        mTestDbHelper.create(expectedMember, Member.class);

        mDatabaseUpgrader.getMemberIdFromMemberName(testMemberName, groupId);
        // check that the member have been created
        List<Member> membersTestResult = mTestDbHelper.queryAll(Member.class);
        assertEquals(1, membersTestResult.size());
        // we can assume that the entries will be in the same order i.e giver Member is first element
        assertTrue(isMemberEqual(expectedMember, membersTestResult.get(0)));
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
         assertTrue(isMemberEqual(expectedMember, membersTestResult.get(0)));
    }

    public void testDuplicateAllMembers() {
        getVersion2Tables();

        GroupVersion2Builder groupVersion2Builder = new GroupVersion2Builder();
        GroupVersion2 groupVersion2 = groupVersion2Builder.withName("anotherGroup").build();
        mTestDbHelper.create(groupVersion2, GroupVersion2.class);

        MemberVersion2Builder memberVersion2Builder = new MemberVersion2Builder();
        MemberVersion2 memberVersionA = memberVersion2Builder.withName("memberA")
                .withContactMode(0)
                .withGroup(groupVersion2).build();
        MemberVersion2 memberVersionB = memberVersion2Builder
                .withName("memberB")
                .withContactMode(1)
                .withGroup(groupVersion2).build();
        MemberVersion2 memberVersionC = memberVersion2Builder
                .withName("memberC")
                .withContactMode(2)
                .withGroup(groupVersion2).build();

        mTestDbHelper.create(memberVersionA, MemberVersion2.class);
        mTestDbHelper.create(memberVersionB, MemberVersion2.class);
        mTestDbHelper.create(memberVersionC, MemberVersion2.class);

        Group.GroupBuilder groupBuilder = new Group.GroupBuilder();
        Group expectedGroup = groupBuilder.build();
        long newGroupId = mTestDbHelper.create(expectedGroup, Group.class);

        mDatabaseUpgrader.duplicateAllMembers(memberVersionA.getGroupId(), newGroupId);
        List<Member> duplicatedMembers = mTestDbHelper.queryAll(Member.class);


        Member.MemberBuilder memberBuilderA = new Member.MemberBuilder();
        Member expectedMemberA = memberBuilderA
                .withName("memberA")
                .withContactMethod(ContactMethod.REVEAL_ONLY)
                .withLookupKey(memberVersionA.getLookupKey())
                .withContactDetails(memberVersionA.getContactDetail())
                .withGroup(expectedGroup)
                .build();

        Member.MemberBuilder memberBuilderB = new Member.MemberBuilder();
        Member expectedMemberB = memberBuilderB
                .withName("memberB")
                .withContactDetails(memberVersionB.getContactDetail())
                .withLookupKey(memberVersionB.getLookupKey())
                .withContactMethod(ContactMethod.SMS)
                .withGroup(expectedGroup).build();

        Member.MemberBuilder memberBuilderC = new Member.MemberBuilder();
        Member expectedMemberC = memberBuilderC
                .withName("memberC")
                .withLookupKey(memberVersionC.getLookupKey())
                .withContactDetails(memberVersionC.getContactDetail())
                .withContactMethod(ContactMethod.EMAIL)
                .withGroup(expectedGroup).build();

        assertEquals(3, duplicatedMembers.size());
        assertTrue(isMemberEqual(expectedMemberA, duplicatedMembers.get(0)));
        assertTrue(isMemberEqual(expectedMemberB, duplicatedMembers.get(1)));
        assertTrue(isMemberEqual(expectedMemberC, duplicatedMembers.get(2)));
    }

    // TODO fix exception constraint failed on insert into restrictions for some reasons!!!
//    public void testDuplicateAllMembersAndRestrictions() {
//
//        getVersion2Tables();
//
//        GroupVersion2Builder groupVersion2Builder = new GroupVersion2Builder();
//        GroupVersion2 groupVersion2 = groupVersion2Builder.withName("Group").build();
//        long oldGroupId = mTestDbHelper.create(groupVersion2, GroupVersion2.class);
//
//        MemberVersion2Builder memberVersion2Builder = new MemberVersion2Builder();
//        MemberVersion2 memberA = memberVersion2Builder
//                .withGroup(groupVersion2)
//                .withName("MemberA")
//                .withContactMode(0)
//                .build();
//        MemberVersion2 memberB = memberVersion2Builder
//                .withGroup(groupVersion2)
//                .withName("MemberB")
//                .withContactMode(1)
//                .build();
//        mTestDbHelper.create(memberA, MemberVersion2.class);
//        mTestDbHelper.create(memberB, MemberVersion2.class);
//
//        try {
//            TableUtils.createTable(mTestDbHelper.getConnectionSource(), RestrictionVersion2.class);
//        } catch (SQLException e) {
//            fail("Creating RestrictionVersion2 Table should not have failed. " + e.getMessage());
//        }
//
//        // insert old restrictions
//        RestrictionVersion2Builder rBuilder = new RestrictionVersion2Builder();
//        RestrictionVersion2 oldRestriction = rBuilder.withMember(memberA).withOtherMember(memberB).build();
//        mTestDbHelper.create(oldRestriction, RestrictionVersion2.class);
//
//        Group.GroupBuilder gBuilder = new Group.GroupBuilder();
//        Group expectedGroup = gBuilder.build();
//        long newGroupId = mTestDbHelper.create(expectedGroup, Group.class);
//        mDatabaseUpgrader.duplicateAllMembers(oldGroupId, newGroupId);
//
//        List<Restriction> restrictions = mTestDbHelper.queryAll(Restriction.class);
//        Restriction newRestriction = restrictions.get(1);
//        assertEquals(3, newRestriction.getMemberId());
//        assertEquals(4, newRestriction.getOtherMemberId());
//    }

    /**********************************************************************************************
     *
     * there are some enforced constraints in the builders but we are responsible to ensure
     * the integrity of the test db we are manually creating for things like no two same member
     * name within the same group for instance.
     * ********************************************************************************************/

    // no restrictions
    // one simple draw result with two members one group
    public void testMigrateDataSimpleTestA() {

        // create an old db
        getVersion2Tables();

        String MEMBER_A_NAME = "memberAName";
        String MEMBER_B_NAME = "memberBName";
        String GROUPNAME = "groupName";
        String MESSAGE = "this is the message";
        long DRAW_DATE = 12345;
        GroupVersion2Builder groupV2Builder = new GroupVersion2Builder();
        GroupVersion2 group = groupV2Builder.withName(GROUPNAME)
                                            .build();
        long groupId = mTestDbHelper.create(group, GroupVersion2.class);

        // has a constraint on group, so need to set it, also on contact_mode
        MemberVersion2 memberA = new MemberVersion2();
        memberA.setName(MEMBER_A_NAME);
        memberA.setGroup(group);
        memberA.setContactMode(0);

        MemberVersion2 memberB = new MemberVersion2();
        memberB.setName(MEMBER_B_NAME);
        memberB.setGroup(group);
        memberA.setContactMode(1);

        DrawResultVersion2Builder drawResultV2Builder = new DrawResultVersion2Builder();
        DrawResultVersion2 drawResult = drawResultV2Builder.withGroup(group)
                                                            .withDrawDate(DRAW_DATE)
                                                            .withMessage(MESSAGE)
                                                            .build();

        // we can use the same builder here where we override all the fields
        DrawResultEntryVersion2Builder drawResultEntryV2Builder = new DrawResultEntryVersion2Builder();
        DrawResultEntryVersion2 drawResultEntryA = drawResultEntryV2Builder.withGiverName(MEMBER_B_NAME)
                                                                      .withReceiverName(MEMBER_A_NAME)
                                                                      .withDrawResult(drawResult)
                                                                        .build();
        DrawResultEntryVersion2 drawResultEntryB = drawResultEntryV2Builder.withGiverName(MEMBER_A_NAME)
                                                                      .withReceiverName(MEMBER_B_NAME)
                                                                      .withDrawResult(drawResult)
                                                                        .build();

        long memberAId = mTestDbHelper.create(memberA, MemberVersion2.class);
        long memberBId = mTestDbHelper.create(memberB, MemberVersion2.class);
        mTestDbHelper.create(drawResult, DrawResultVersion2.class);
        mTestDbHelper.create(drawResultEntryA, DrawResultEntryVersion2.class);
        mTestDbHelper.create(drawResultEntryB, DrawResultEntryVersion2.class);

        mDatabaseUpgrader.migrateDataToVersion3AssignmentsTable(mTestDbHelper.getWritableDatabase());

        // query new tables to check results

        // check group migration
        Group migratedGroup = mTestDbHelper.queryById(groupId, Group.class);

        Group expectedGroup = new Group();
        expectedGroup.setName(GROUPNAME + " #1" + GROUP_MIGRATED);
        expectedGroup.setMessage(MESSAGE);
        expectedGroup.setDrawDate(DRAW_DATE);
        assertTrue(isGroupEqual(expectedGroup, migratedGroup));

        // check member migration and assignment migration
        Member migratedMemberA = mTestDbHelper.queryById(memberAId, Member.class);
        Member migratedMemberB = mTestDbHelper.queryById(memberBId, Member.class);
        Assignment expectedAssignmentA = new Assignment();
        expectedAssignmentA.setReceiverMember(migratedMemberA);
        expectedAssignmentA.setGiverMember(migratedMemberB);
        expectedAssignmentA.setSendStatus(Assignment.Status.Assigned);

        Assignment expectedAssignmentB = new Assignment();
        expectedAssignmentB.setReceiverMember(migratedMemberB);
        expectedAssignmentB.setGiverMember(migratedMemberA);
        expectedAssignmentB.setSendStatus(Assignment.Status.Assigned);

        List<Assignment> testResultsAssignments = mTestDbHelper.queryAll(Assignment.class);
        assertEquals(2, testResultsAssignments.size());
        assertTrue(isAssignmentInsideList(expectedAssignmentA, testResultsAssignments));
        assertTrue(isAssignmentInsideList(expectedAssignmentB, testResultsAssignments));
    }

    // test all model classes migrations
//    public void testMigrateDataAllModelClassesMigrations() {
//
//        // create an old db
//        Class[] PERSISTABLE_OBJECTS = new Class[]
//                { GroupVersion2.class, MemberVersion2.class, DrawResultVersion2.class, DrawResultEntryVersion2.class, RestrictionVersion2.class};
//
//        mTestDbHelper = new TestDatabaseHelper(getContext(), TEST_DATABASE_NAME, PERSISTABLE_OBJECTS);
//        mTestDbHelper.getWritableDatabase().beginTransaction();
//
//        mDatabaseUpgrader = new DatabaseUpgrader(mTestDbHelper);
//
//        // we need to run the alter queries so as to get the new table version3 setup for these tests
//        mDatabaseUpgrader.addNewAssignmentTable(mTestDbHelper.mConnectionSource);
//
//        String MEMBER_A_NAME = "memberAName";
//        String MEMBER_B_NAME = "memberBName";
//        String MEMBER_C_NAME = "memberCName";
//
//        String MEMBER_A_CONTACT_DETAILS = "memberAContactDetails";
//        String MEMBER_B_CONTACT_DETAILS = "memberBContactDetails";
//        // no contact detail for member c (view on phone option)
//
//        String MEMBER_A_LOOKUP_KEY = "memberALookupKey";
//        String MEMBER_B_LOOKUP_KEY = "memberBLookupKey";
//        // no lookup key for member c (manual entry)
//
//        // we don't migrate the contact mode info so this doesn't really matter
//        int MEMBER_A_CONTACT_MODE = ConstantsVersion2.EMAIL_CONTACT_MODE;
//        int MEMBER_B_CONTACT_MODE = ConstantsVersion2.SMS_CONTACT_MODE;
//        int MEMBER_C_CONTACT_MODE = ConstantsVersion2.NAME_ONLY_CONTACT_MODE;
//
//        long MEMBER_A_VIEWED_DATE = 90;
//        long MEMBER_A_SENT_DATE = 100;
//        long MEMBER_B_VIEWED_DATE = 120;
//
//        String SCHOOL_GROUP = "schoolGroup";
//        String SCHOOL_MESSAGE = "this is the message for the school group";
//        long SCHOOL_DRAW_DATE = 12;
//        long SCHOOL_SEND_DATE = 54;
//
//        // create groups
//        GroupVersion2Builder groupV2Builder = new GroupVersion2Builder();
//        GroupVersion2 schoolGroup = groupV2Builder.withName(SCHOOL_GROUP)
//                                         .build();
//
//        long schoolGroupId = mTestDbHelper.create(schoolGroup, GroupVersion2.class);
//
//        // has a constraint on group, so need to set group
//        MemberVersion2 memberA = new MemberVersion2();
//        memberA.setName(MEMBER_A_NAME);
//        memberA.setContactDetail(MEMBER_A_CONTACT_DETAILS);
//        memberA.setLookupKey(MEMBER_A_LOOKUP_KEY);
//        memberA.setContactMode(MEMBER_A_CONTACT_MODE);
//        memberA.setGroup(schoolGroup);
//
//        MemberVersion2 memberB = new MemberVersion2();
//        memberB.setName(MEMBER_B_NAME);
//        memberB.setContactDetail(MEMBER_B_CONTACT_DETAILS);
//        memberB.setLookupKey(MEMBER_B_LOOKUP_KEY);
//        memberB.setContactMode(MEMBER_B_CONTACT_MODE);
//        memberB.setGroup(schoolGroup);
//
//        MemberVersion2 memberC = new MemberVersion2();
//        memberC.setName(MEMBER_C_NAME);
//        memberC.setContactMode(MEMBER_C_CONTACT_MODE);
//        memberC.setGroup(schoolGroup);
//
//        // create members
//        long memberAId = mTestDbHelper.create(memberA, MemberVersion2.class);
//        long memberBId = mTestDbHelper.create(memberB, MemberVersion2.class);
//        mTestDbHelper.create(memberC, MemberVersion2.class);
//
//        RestrictionVersion2Builder restrictionVersion2Builder = new RestrictionVersion2Builder();
//        RestrictionVersion2 restrictionVersion2 = restrictionVersion2Builder.build();
//        mTestDbHelper.create(restrictionVersion2, RestrictionVersion2.class);
//
//        // member A can't give to B
//        RestrictionVersion2Builder restrictionV2Builder = new RestrictionVersion2Builder();
//        RestrictionVersion2 restrictionAOne = restrictionV2Builder.withMember(memberA)
//                                                                    .withOtherMember(memberB)
//                                                                    .build();
//
//        // create restrictions
//        long restrictionAOneId = mTestDbHelper.create(restrictionAOne, RestrictionVersion2.class);
//
//        DrawResultVersion2Builder drawResultV2Builder = new DrawResultVersion2Builder();
//        DrawResultVersion2 drawResult = drawResultV2Builder
//                .withGroup(schoolGroup)
//                .withDrawDate(SCHOOL_DRAW_DATE)
//                .withMessage(SCHOOL_MESSAGE)
//                .withSendDate(SCHOOL_SEND_DATE)
//                .build();
//
//        // we can't use the builder here as we are not reallocating all the properties
//        // we need to recreate instances
//
//        // B -> A
//        DrawResultEntryVersion2Builder drawResultEntryV2BuilderA = new DrawResultEntryVersion2Builder();
//        DrawResultEntryVersion2 drawResultEntryA = drawResultEntryV2BuilderA
//                .withGiverName(MEMBER_B_NAME)
//                .withReceiverName(MEMBER_A_NAME)
//                .withDrawResult(drawResult)
//                .withContactMode(MEMBER_A_CONTACT_MODE)
//                .withContactDetails(MEMBER_A_CONTACT_DETAILS)
//                .withViewedDate(MEMBER_A_VIEWED_DATE)
//                .withSentDate(MEMBER_A_SENT_DATE)
//                .build();
//
//        // C -> B
//        DrawResultEntryVersion2Builder drawResultEntryV2BuilderB = new DrawResultEntryVersion2Builder();
//        DrawResultEntryVersion2 drawResultEntryB = drawResultEntryV2BuilderB
//                .withGiverName(MEMBER_C_NAME)
//                .withReceiverName(MEMBER_B_NAME)
//                .withDrawResult(drawResult)
//                .withContactMode(MEMBER_B_CONTACT_MODE)
//                .withContactDetails(MEMBER_B_CONTACT_DETAILS)
//                .withViewedDate(MEMBER_B_VIEWED_DATE)
//                .build();
//
//        // A -> C
//        DrawResultEntryVersion2Builder drawResultEntryV2BuilderC = new DrawResultEntryVersion2Builder();
//        DrawResultEntryVersion2 drawResultEntryC = drawResultEntryV2BuilderC
//                .withGiverName(MEMBER_A_NAME)
//                .withReceiverName(MEMBER_C_NAME)
//                .withDrawResult(drawResult)
//                .withContactMode(MEMBER_C_CONTACT_MODE)
//                .build();
//
//        // create draw result and draw result entries
//        mTestDbHelper.create(drawResult, DrawResultVersion2.class);
//        mTestDbHelper.create(drawResultEntryA, DrawResultEntryVersion2.class);
//        mTestDbHelper.create(drawResultEntryB, DrawResultEntryVersion2.class);
//        mTestDbHelper.create(drawResultEntryC, DrawResultEntryVersion2.class);
//
//        mDatabaseUpgrader.createNewMemberVersion3Table(mTestDbHelper.mConnectionSource);
//        mDatabaseUpgrader.migrateMemberAndRestrictionsTable(mTestDbHelper.mConnectionSource);
//        mDatabaseUpgrader.alterGroupTable();
//
//        // run migration
//        mDatabaseUpgrader.migrateDataToVersion3AssignmentsTable(mTestDbHelper.getWritableDatabase());
//
//        // query new tables to check results
//
//        // check group migration
//        Group migratedGroup = mTestDbHelper.queryById(schoolGroupId, Group.class);
//        Group.GroupBuilder groupBuilder = new Group.GroupBuilder();
//        Group expectedSchoolGroup = groupBuilder
//                .withGroupId(schoolGroupId)
//                .withName(SCHOOL_GROUP + " #1" + GROUP_MIGRATED)
//                .withMessage(SCHOOL_MESSAGE)
//                .withDrawDate(SCHOOL_DRAW_DATE)
//                .build();
//
//        assertTrue(isGroupEqual(migratedGroup, expectedSchoolGroup));
//
//        // check member migration
//        // because to check if assignments are correct, we need the member id, we have to go query and set the correct id
//        // for that
//        long expectedMemberAId = mDatabaseUpgrader.getMemberIdFromMemberName(MEMBER_A_NAME, migratedGroup.getId());
//
//        Member.MemberBuilder memberBuilderA = new Member.MemberBuilder();
//        Member expectedMemberA = memberBuilderA.withMemberId(expectedMemberAId)
//                .withLookupKey(MEMBER_A_LOOKUP_KEY)
//                .withName(MEMBER_A_NAME)
//                .withContactDetails(MEMBER_A_CONTACT_DETAILS)
//                .withContactMethod(ContactMethod.EMAIL)
//                .withGroup(expectedSchoolGroup)
//                .build();
//
//        // all migrated members are expected to have the contact method set to reveal_only which gets set as default
//
//        long expectedMemberBId = mDatabaseUpgrader.getMemberIdFromMemberName(MEMBER_B_NAME, migratedGroup.getId());
//
//        Member.MemberBuilder memberBuilderB = new Member.MemberBuilder();
//        Member expectedMemberB = memberBuilderB.withMemberId(expectedMemberBId)
//                .withLookupKey(MEMBER_B_LOOKUP_KEY)
//                .withName(MEMBER_B_NAME)
//                .withContactDetails(MEMBER_B_CONTACT_DETAILS)
//                .withContactMethod(ContactMethod.SMS)
//                .withGroup(expectedSchoolGroup)
//                .build();
//
//        long expectedMemberCId = mDatabaseUpgrader.getMemberIdFromMemberName(MEMBER_C_NAME, migratedGroup.getId());
//
//        Member.MemberBuilder memberBuilderC = new Member.MemberBuilder();
//        Member expectedMemberC = memberBuilderC.withMemberId(expectedMemberCId)
//                .withName(MEMBER_C_NAME)
//                .withGroup(expectedSchoolGroup)
//                .build();
//
//        // check restriction migration
////        Restriction migratedRestriction = mTestDbHelper.queryById(restrictionAOneId, Restriction.class);
////        assertEquals(memberAId, migratedRestriction.getMemberId());
////        assertEquals(memberBId, migratedRestriction.getOtherMemberId());
//
//        // B -> A and assignemnt has been sent
//        Assignment expectedAssignmentA = new Assignment();
//        expectedAssignmentA.setReceiverMember(expectedMemberA);
//        expectedAssignmentA.setGiverMember(mTestDbHelper.queryById(memberBId, Member.class));
//        expectedAssignmentA.setSendStatus(Assignment.Status.Sent);
//
//        // C -> B and assignemnt has been viewed
//        Assignment expectedAssignmentB = new Assignment();
//        expectedAssignmentB.setReceiverMember(expectedMemberB);
//        expectedAssignmentB.setGiverMember(expectedMemberC);
//        expectedAssignmentB.setSendStatus(Assignment.Status.Revealed);
//
//        // A -> C and assignment has just been assigned
//        Assignment expectedAssignmentC = new Assignment();
//        expectedAssignmentC.setReceiverMember(expectedMemberC);
//        expectedAssignmentC.setGiverMember(expectedMemberA);
//        expectedAssignmentC.setSendStatus(Assignment.Status.Assigned);
//
//        // check member migration
//        // as members are inserted by code, we don't know the id, so let's query all
//        List<Member> migratedMembers = mTestDbHelper.queryAll(Member.class);
//        assertEquals(3, migratedMembers.size());
//        assertTrue(isMemberInsideList(expectedMemberA, migratedMembers));
//        assertTrue(isMemberInsideList(expectedMemberB, migratedMembers));
//        assertTrue(isMemberInsideList(expectedMemberC, migratedMembers));
//
//        // check assignment migration
//        List<Assignment> testResultsAssignments = mTestDbHelper.queryAll(Assignment.class);
//        assertEquals(3, testResultsAssignments.size());
//
//        assertTrue(isAssignmentInsideList(expectedAssignmentA, testResultsAssignments));
//        assertTrue(isAssignmentInsideList(expectedAssignmentB, testResultsAssignments));
//        assertTrue(isAssignmentInsideList(expectedAssignmentC, testResultsAssignments));
//    }

    public void testApostropheName() {
        getVersion3Tables();
        mDatabaseUpgrader.getMemberIdFromMemberName("Two can't give to one", 13);

        // test that it can insert!
    }
}
