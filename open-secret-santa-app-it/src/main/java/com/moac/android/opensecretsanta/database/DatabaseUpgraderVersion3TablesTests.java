package com.moac.android.opensecretsanta.database;

import android.test.AndroidTestCase;
import com.moac.android.opensecretsanta.builders.DrawResultEntryVersion2Builder;
import com.moac.android.opensecretsanta.builders.DrawResultVersion2Builder;
import com.moac.android.opensecretsanta.builders.GroupVersion2Builder;
import com.moac.android.opensecretsanta.builders.RestrictionVersion2Builder;
import com.moac.android.opensecretsanta.model.*;
import com.moac.android.opensecretsanta.model.version2.*;

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
        mDatabaseUpgrader.addNewAssignmentTable(mTestDbHelper.mConnectionSource);
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
        mTestDbHelper.create(expectedDrawResult, DrawResultVersion2.class);

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
        mTestDbHelper.create(expectedDrawResult, DrawResultVersion2.class);

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
        long expectedGiverMemberId = mTestDbHelper.create(giver, MemberVersion2.class);

        MemberVersion2 receiver = new MemberVersion2();
        receiver.setName(expectedReceiverName);
        receiver.setGroup(expectedGroup);
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

        String expectedGroupName = groupName + GROUP_MIGRATED;
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

        mDatabaseUpgrader.updateMigratedGroupInfo(drawResultVersion2, groupName);

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
        expectedMember.setContactDetails("member@me.com");
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

        // has a constraint on group, so need to set it
        MemberVersion2 memberA = new MemberVersion2();
        memberA.setName(MEMBER_A_NAME);
        memberA.setGroup(group);

        MemberVersion2 memberB = new MemberVersion2();
        memberB.setName(MEMBER_B_NAME);
        memberB.setGroup(group);

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

        mDatabaseUpgrader.migrateDataToVersion3AssignmentsTable();

        // query new tables to check results

        // check group migration
        Group migratedGroup = mTestDbHelper.queryById(groupId, Group.class);

        Group expectedGroup = new Group();
        expectedGroup.setName(GROUPNAME + GROUP_MIGRATED);
        expectedGroup.setMessage(MESSAGE);
        expectedGroup.setDrawDate(DRAW_DATE);
        assertEquals(expectedGroup, migratedGroup);

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
        assertTrue(testResultsAssignments.contains(expectedAssignmentA));
        assertTrue(testResultsAssignments.contains(expectedAssignmentB));
    }


    // test all model classes migrations
    public void testMigrateDataAllModelClassesMigrations() {

        // create an old db
        getVersion2Tables();

        String MEMBER_A_NAME = "memberAName";
        String MEMBER_B_NAME = "memberBName";
        String MEMBER_C_NAME = "memberCName";

        String MEMBER_A_CONTACT_DETAILS = "memberAContactDetails";
        String MEMBER_B_CONTACT_DETAILS = "memberBContactDetails";
        // no contact detail for member c (view on phone option)

        String MEMBER_A_LOOKUP_KEY = "memberALookupKey";
        String MEMBER_B_LOOKUP_KEY = "memberBLookupKey";
        // no lookup key for member c (manual entry)

        // we don't migrate the contact mode info so this doesn't really matter
        int MEMBER_A_CONTACT_MODE = ConstantsVersion2.EMAIL_CONTACT_MODE;
        int MEMBER_B_CONTACT_MODE = ConstantsVersion2.SMS_CONTACT_MODE;
        int MEMBER_C_CONTACT_MODE = ConstantsVersion2.NAME_ONLY_CONTACT_MODE;

        long MEMBER_A_VIEWED_DATE = 90;
        long MEMBER_A_SENT_DATE = 100;
        long MEMBER_B_VIEWED_DATE = 120;

        String SCHOOL_GROUP = "schoolGroup";
        String SCHOOL_MESSAGE = "this is the message for the school group";
        long SCHOOL_DRAW_DATE = 12;
        long SCHOOL_SEND_DATE = 54;

        // create groups
        GroupVersion2Builder groupV2Builder = new GroupVersion2Builder();
        GroupVersion2 schoolGroup = groupV2Builder.withName(SCHOOL_GROUP)
                                         .build();

        long schoolGroupId = mTestDbHelper.create(schoolGroup, GroupVersion2.class);

        // has a constraint on group, so need to set group
        MemberVersion2 memberA = new MemberVersion2();
        memberA.setName(MEMBER_A_NAME);
        memberA.setContactDetail(MEMBER_A_CONTACT_DETAILS);
        memberA.setLookupKey(MEMBER_A_LOOKUP_KEY);
        memberA.setContactMode(MEMBER_A_CONTACT_MODE);
        memberA.setGroup(schoolGroup);

        MemberVersion2 memberB = new MemberVersion2();
        memberB.setName(MEMBER_B_NAME);
        memberB.setContactDetail(MEMBER_B_CONTACT_DETAILS);
        memberB.setLookupKey(MEMBER_B_LOOKUP_KEY);
        memberB.setContactMode(MEMBER_B_CONTACT_MODE);
        memberB.setGroup(schoolGroup);

        MemberVersion2 memberC = new MemberVersion2();
        memberC.setName(MEMBER_C_NAME);
        memberC.setContactMode(MEMBER_C_CONTACT_MODE);
        memberC.setGroup(schoolGroup);

        // create members
        long memberAId = mTestDbHelper.create(memberA, MemberVersion2.class);
        long memberBId = mTestDbHelper.create(memberB, MemberVersion2.class);
        mTestDbHelper.create(memberC, MemberVersion2.class);

        // member A can't give to B
        RestrictionVersion2Builder restrictionV2Builder = new RestrictionVersion2Builder();
        RestrictionVersion2 restrictionAOne = restrictionV2Builder.withMember(memberA)
                                                                    .withOtherMember(memberB)
                                                                    .build();

        // create restrictions
        long restrictionAOneId = mTestDbHelper.create(restrictionAOne, RestrictionVersion2.class);

        DrawResultVersion2Builder drawResultV2Builder = new DrawResultVersion2Builder();
        DrawResultVersion2 drawResult = drawResultV2Builder
                .withGroup(schoolGroup)
                .withDrawDate(SCHOOL_DRAW_DATE)
                .withMessage(SCHOOL_MESSAGE)
                .withSendDate(SCHOOL_SEND_DATE)
                .build();

        // we can't use the builder here as we are not reallocating all the properties
        // we need to recreate instances

        // B -> A
        DrawResultEntryVersion2Builder drawResultEntryV2BuilderA = new DrawResultEntryVersion2Builder();
        DrawResultEntryVersion2 drawResultEntryA = drawResultEntryV2BuilderA
                .withGiverName(MEMBER_B_NAME)
                .withReceiverName(MEMBER_A_NAME)
                .withDrawResult(drawResult)
                .withContactMode(MEMBER_A_CONTACT_MODE)
                .withContactDetails(MEMBER_A_CONTACT_DETAILS)
                .withViewedDate(MEMBER_A_VIEWED_DATE)
                .withSentDate(MEMBER_A_SENT_DATE)
                .build();

        // C -> B
        DrawResultEntryVersion2Builder drawResultEntryV2BuilderB = new DrawResultEntryVersion2Builder();
        DrawResultEntryVersion2 drawResultEntryB = drawResultEntryV2BuilderB
                .withGiverName(MEMBER_C_NAME)
                .withReceiverName(MEMBER_B_NAME)
                .withDrawResult(drawResult)
                .withContactMode(MEMBER_B_CONTACT_MODE)
                .withContactDetails(MEMBER_B_CONTACT_DETAILS)
                .withViewedDate(MEMBER_B_VIEWED_DATE)
                .build();

        // A -> C
        DrawResultEntryVersion2Builder drawResultEntryV2BuilderC = new DrawResultEntryVersion2Builder();
        DrawResultEntryVersion2 drawResultEntryC = drawResultEntryV2BuilderC
                .withGiverName(MEMBER_A_NAME)
                .withReceiverName(MEMBER_C_NAME)
                .withDrawResult(drawResult)
                .withContactMode(MEMBER_C_CONTACT_MODE)
                .build();

        // create draw result and draw result entries
        mTestDbHelper.create(drawResult, DrawResultVersion2.class);
        mTestDbHelper.create(drawResultEntryA, DrawResultEntryVersion2.class);
        mTestDbHelper.create(drawResultEntryB, DrawResultEntryVersion2.class);
        mTestDbHelper.create(drawResultEntryC, DrawResultEntryVersion2.class);

        // run migration
        mDatabaseUpgrader.migrateDataToVersion3AssignmentsTable();

        // query new tables to check results

        // check group migration
        Group migratedGroup = mTestDbHelper.queryById(schoolGroupId, Group.class);
        Group.GroupBuilder groupBuilder = new Group.GroupBuilder();
        Group expectedSchoolGroup = groupBuilder
                .withGroupId(schoolGroupId)
                .withName(SCHOOL_GROUP + GROUP_MIGRATED)
                .withMessage(SCHOOL_MESSAGE)
                .withDrawDate(SCHOOL_DRAW_DATE)
                .build();

        assertEquals(migratedGroup, expectedSchoolGroup);

        // check member migration
        // because to check if assignments are correct, we need the member id, we have to go query and set the correct id
        // for that
        long expectedMemberAId = mDatabaseUpgrader.getMemberIdFromMemberName(MEMBER_A_NAME, migratedGroup.getId());

        Member.MemberBuilder memberBuilderA = new Member.MemberBuilder();
        Member expectedMemberA = memberBuilderA.withMemberId(expectedMemberAId)
                .withLookupKey(MEMBER_A_LOOKUP_KEY)
                .withName(MEMBER_A_NAME)
                .withContactDetails(MEMBER_A_CONTACT_DETAILS)
                .withGroup(expectedSchoolGroup)
                .build();

        // all migrated members are expected to have the contact method set to reveal_only which gets set as default

        long expectedMemberBId = mDatabaseUpgrader.getMemberIdFromMemberName(MEMBER_B_NAME, migratedGroup.getId());

        Member.MemberBuilder memberBuilderB = new Member.MemberBuilder();
        Member expectedMemberB = memberBuilderB.withMemberId(expectedMemberBId)
                .withLookupKey(MEMBER_B_LOOKUP_KEY)
                .withName(MEMBER_B_NAME)
                .withContactDetails(MEMBER_B_CONTACT_DETAILS)
                .withGroup(expectedSchoolGroup)
                .build();

        long expectedMemberCId = mDatabaseUpgrader.getMemberIdFromMemberName(MEMBER_C_NAME, migratedGroup.getId());

        Member.MemberBuilder memberBuilderC = new Member.MemberBuilder();
        Member expectedMemberC = memberBuilderC.withMemberId(expectedMemberCId)
                .withName(MEMBER_C_NAME)
                .withGroup(expectedSchoolGroup)
                .build();

        // check restriction migration
        Restriction migratedRestriction = mTestDbHelper.queryById(restrictionAOneId, Restriction.class);
        assertEquals(memberAId, migratedRestriction.getMemberId());
        assertEquals(memberBId, migratedRestriction.getOtherMemberId());

        // B -> A and assignemnt has been sent
        Assignment expectedAssignmentA = new Assignment();
        expectedAssignmentA.setReceiverMember(expectedMemberA);
        expectedAssignmentA.setGiverMember(mTestDbHelper.queryById(memberBId, Member.class));
        expectedAssignmentA.setSendStatus(Assignment.Status.Sent);

        // C -> B and assignemnt has been viewed
        Assignment expectedAssignmentB = new Assignment();
        expectedAssignmentB.setReceiverMember(expectedMemberB);
        expectedAssignmentB.setGiverMember(expectedMemberC);
        expectedAssignmentB.setSendStatus(Assignment.Status.Revealed);

        // A -> C and assignment has just been assigned
        Assignment expectedAssignmentC = new Assignment();
        expectedAssignmentC.setReceiverMember(expectedMemberC);
        expectedAssignmentC.setGiverMember(expectedMemberA);
        expectedAssignmentC.setSendStatus(Assignment.Status.Assigned);

        // check member migration
        // as members are inserted by code, we don't know the id, so let's query all
        List<Member> migratedMembers = mTestDbHelper.queryAll(Member.class);
        assertEquals(3, migratedMembers.size());
        assertTrue(migratedMembers.contains(expectedMemberA));
        assertTrue(migratedMembers.contains(expectedMemberB));
        assertTrue(migratedMembers.contains(expectedMemberC));

        // check assignment migration
        List<Assignment> testResultsAssignments = mTestDbHelper.queryAll(Assignment.class);
        assertEquals(3, testResultsAssignments.size());

        assertTrue(testResultsAssignments.contains(expectedAssignmentA));
        assertTrue(testResultsAssignments.contains(expectedAssignmentB));
        assertTrue(testResultsAssignments.contains(expectedAssignmentC));
    }

     // TODO take in a real db
     // TODO multiple groups
     // TODO just groups no draw results

     // TODO test with deleted members
}
