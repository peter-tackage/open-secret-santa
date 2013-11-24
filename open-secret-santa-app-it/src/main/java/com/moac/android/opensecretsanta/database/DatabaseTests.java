package com.moac.android.opensecretsanta.database;

import android.database.SQLException;
import android.test.AndroidTestCase;
import com.moac.android.opensecretsanta.builders.GroupBuilder;
import com.moac.android.opensecretsanta.builders.MemberBuilder;
import com.moac.android.opensecretsanta.model.*;

import java.util.List;

public class DatabaseTests extends AndroidTestCase {

    private static final String TEST_DATABASE_NAME = "testopensecretsanta.db";
    DatabaseManager mDatabaseManager;
    TestDatabaseHelper mDbHelper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Class[] PERSISTABLE_OBJECTS = new Class[]{ Group.class, Member.class, Restriction.class, Assignment.class };
        mDbHelper = new TestDatabaseHelper(getContext(), TEST_DATABASE_NAME, PERSISTABLE_OBJECTS);
        mDatabaseManager = new DatabaseManager(mDbHelper);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        getContext().deleteDatabase(TEST_DATABASE_NAME);
        mDatabaseManager = null;
    }

    /*
     * Bespoke Query Tests
     */

    public void testDeleteRestrictionBetweenMembers() {
        // Add a Group
        Group group1 = new GroupBuilder().build();
        mDatabaseManager.create(group1);

        // Add some Members
        Member m1 = new MemberBuilder().build();
        Member m2 = new MemberBuilder().withName("m2").build();
        Member m3 = new MemberBuilder().withName("m3").build();
        m1.setGroup(group1);
        m2.setGroup(group1);
        m3.setGroup(group1);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);
        mDatabaseManager.create(m3);

        // Add Restrictions
        Restriction m1m2 = new Restriction();
        m1m2.setMember(m1);
        m1m2.setOtherMember(m2);
        long r1Id = mDatabaseManager.create(m1m2);
        assertTrue(r1Id != PersistableObject.UNSET_ID);

        Restriction m1m3 = new Restriction();
        m1m3.setMember(m1);
        m1m3.setOtherMember(m3);
        long r2Id = mDatabaseManager.create(m1m3);
        assertTrue(r2Id != PersistableObject.UNSET_ID);

        long rowsDeleted = mDatabaseManager.deleteRestrictionBetweenMembers(m1.getId(), m2.getId());
        assertEquals(1, rowsDeleted);
        assertNull(mDatabaseManager.queryById(r1Id, Restriction.class));
        assertNotNull(mDatabaseManager.queryById(r2Id, Restriction.class));
    }

    public void testQueryAllRestrictionsForMemberId() {
        // Add a Group
        Group group1 = new GroupBuilder().build();
        mDatabaseManager.create(group1);

        // Add some Members
        Member m1 = new MemberBuilder().build();
        Member m2 = new MemberBuilder().withName("m2").build();
        Member m3 = new MemberBuilder().withName("m3").build();
        m1.setGroup(group1);
        m2.setGroup(group1);
        m3.setGroup(group1);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);
        mDatabaseManager.create(m3);

        // Add Restrictions
        Restriction m1m2 = new Restriction();
        m1m2.setMember(m1);
        m1m2.setOtherMember(m2);
        mDatabaseManager.create(m1m2);

        Restriction m1m3 = new Restriction();
        m1m3.setMember(m1);
        m1m3.setOtherMember(m3);
        mDatabaseManager.create(m1m3);

        // Query
        List<Restriction> restrictionsM1 = mDatabaseManager.queryAllRestrictionsForMemberId(m1.getId());

        // Verify restrictions returned
        assertEquals(2, restrictionsM1.size());
    }

    public void testQueryIsRestricted() {
        // Add a Group
        Group group1 = new GroupBuilder().build();
        mDatabaseManager.create(group1);

        // Add some Members
        Member m1 = new MemberBuilder().build();
        Member m2 = new MemberBuilder().withName("m2").build();
        Member m3 = new MemberBuilder().withName("m3").build();
        m1.setGroup(group1);
        m2.setGroup(group1);
        m3.setGroup(group1);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);
        mDatabaseManager.create(m3);

        // Add Restrictions
        Restriction m1m2 = new Restriction();
        m1m2.setMember(m1);
        m1m2.setOtherMember(m2);
        mDatabaseManager.create(m1m2);

        // Verify the restrictions
        assertTrue(mDatabaseManager.queryIsRestricted(m1.getId(), m2.getId()));
        assertTrue(!mDatabaseManager.queryIsRestricted(m1.getId(), m3.getId()));
    }

    public void testQueryAllMembersForGroup() {
        // Add a Group
        // Add some members
        // Query and verify
        // Add a Group
        Group group1 = new GroupBuilder().build();
        Group group2 = new GroupBuilder().withName("g2").build();
        mDatabaseManager.create(group1);
        mDatabaseManager.create(group2);

        // Add some Members
        Member m1 = new MemberBuilder().build();
        Member m2 = new MemberBuilder().withName("m2").build();
        Member m3 = new MemberBuilder().withName("m3").build();
        Member m4 = new MemberBuilder().withName("m4").build();
        m1.setGroup(group1);
        m2.setGroup(group1);
        m3.setGroup(group1);
        m4.setGroup(group2);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);
        mDatabaseManager.create(m3);
        mDatabaseManager.create(m4);

        List<Member> members1 = mDatabaseManager.queryAllMembersForGroup(group1.getId());
        List<Member> members2 = mDatabaseManager.queryAllMembersForGroup(group2.getId());

        assertEquals(3, members1.size());
        assertEquals(1, members2.size());
    }

    public void testQueryAllMembersForGroupExcept() {
        // Add a Group
        // Add some members
        // Query and verify
        Group group1 = new GroupBuilder().build();
        mDatabaseManager.create(group1);

        // Add some Members
        Member m1 = new MemberBuilder().build();
        Member m2 = new MemberBuilder().withName("m2").build();
        Member m3 = new MemberBuilder().withName("m3").build();
        m1.setGroup(group1);
        m2.setGroup(group1);
        m3.setGroup(group1);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);
        mDatabaseManager.create(m3);

        List<Member> members = mDatabaseManager.queryAllMembersForGroupExcept(group1.getId(), m1.getId());

        assertEquals(2, members.size());
        assertFalse(members.contains(m1));
    }

    public void testQueryMemberWithNameForGroup() {
        // Add a Group
        // Add some members
        // Query and verify
        Group group1 = new GroupBuilder().build();
        mDatabaseManager.create(group1);

        // Add some Members
        Member m1 = new MemberBuilder().withName("m1").build();
        Member m2 = new MemberBuilder().withName("m2").build();
        m1.setGroup(group1);
        m2.setGroup(group1);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);

        Member member = mDatabaseManager.queryMemberWithNameForGroup(group1.getId(), m1.getName());

        assertEquals(m1.getName(), member.getName());
    }

    public void testDeleteAllRestrictionsForMember() {
        // Add a Group
        // Add some Members
        // Add restrictions to member #1
        // Add restrictions to member #2
        // Delete restrictions for member #1.
        // Verify restrictions delete for #1, not #2
        // Add a Group
        Group group1 = new GroupBuilder().build();
        mDatabaseManager.create(group1);

        // Add some Members
        Member m1 = new MemberBuilder().build();
        Member m2 = new MemberBuilder().withName("m2").build();
        Member m3 = new MemberBuilder().withName("m3").build();
        m1.setGroup(group1);
        m2.setGroup(group1);
        m3.setGroup(group1);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);
        mDatabaseManager.create(m3);

        // Add Restrictions
        Restriction m1m2 = new Restriction();
        m1m2.setMember(m1);
        m1m2.setOtherMember(m2);
        mDatabaseManager.create(m1m2);

        Restriction m1m3 = new Restriction();
        m1m2.setMember(m1);
        m1m2.setOtherMember(m3);
        mDatabaseManager.create(m1m3);

        // Add another restriction that won't be deleted.
        Restriction m2m1 = new Restriction();
        m2m1.setMember(m2);
        m2m1.setOtherMember(m1);
        mDatabaseManager.create(m2m1);

        // Delete m1's restrictions
        mDatabaseManager.deleteAllRestrictionsForMember(m1.getId());

        // Verify restrictions correctly deleted
        assertEquals(0, mDatabaseManager.queryAllRestrictionsForMemberId(m1.getId()).size());
        assertEquals(1, mDatabaseManager.queryAllRestrictionsForMemberId(m2.getId()).size());
    }

    public void testQueryAllAssignmentsForGroup() {
        Group group1 = new GroupBuilder().build();
        mDatabaseManager.create(group1);

        // Add some Members
        Member m1 = new MemberBuilder().withName("m1").build();
        Member m2 = new MemberBuilder().withName("m2").build();
        Member m3 = new MemberBuilder().withName("m3").build();
        m1.setGroup(group1);
        m2.setGroup(group1);
        m3.setGroup(group1);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);
        mDatabaseManager.create(m3);

        Assignment a1 = new Assignment();
        a1.setGiverMember(m1);
        a1.setReceiverMember(m2);
        Assignment a2 = new Assignment();
        a2.setGiverMember(m2);
        a2.setReceiverMember(m3);
        Assignment a3 = new Assignment();
        a3.setGiverMember(m3);
        a3.setReceiverMember(m1);
        mDatabaseManager.create(a1);
        mDatabaseManager.create(a2);
        mDatabaseManager.create(a3);

        /**
         * Make a second group - testing query doesn't pull these in.
         */

        Group group2 = new GroupBuilder().withName("group2").build();
        mDatabaseManager.create(group2);

        // Add some Members
        Member m1_2 = new MemberBuilder().withName("m1-2").build();
        Member m2_2 = new MemberBuilder().withName("m2-2").build();
        m1_2.setGroup(group2);
        m2_2.setGroup(group2);
        mDatabaseManager.create(m1_2);
        mDatabaseManager.create(m2_2);

        Assignment a1_2 = new Assignment();
        a1_2.setGiverMember(m1_2);
        a1_2.setReceiverMember(m2_2);
        Assignment a2_2 = new Assignment();
        a2_2.setGiverMember(m2_2);
        a2_2.setReceiverMember(m1_2);

        mDatabaseManager.create(a1_2);
        mDatabaseManager.create(a2_2);

        List<Assignment> assignments = mDatabaseManager.queryAllAssignmentsForGroup(group1.getId());
        assertEquals(3, assignments.size());
        // FIXME This is order dependent - really only care it contains the Assignments.
        assertEquals(a1.getId(), assignments.get(0).getId());
        assertEquals(a2.getId(), assignments.get(1).getId());
        assertEquals(a3.getId(), assignments.get(2).getId());
    }

    public void testDeleteAllAssignmentsForGroup() {
        Group group1 = new GroupBuilder().build();
        mDatabaseManager.create(group1);

        // Add some Members
        Member m1 = new MemberBuilder().withName("m1").build();
        Member m2 = new MemberBuilder().withName("m2").build();
        Member m3 = new MemberBuilder().withName("m3").build();
        m1.setGroup(group1);
        m2.setGroup(group1);
        m3.setGroup(group1);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);
        mDatabaseManager.create(m3);

        Assignment a1 = new Assignment();
        a1.setGiverMember(m1);
        a1.setReceiverMember(m2);
        Assignment a2 = new Assignment();
        a2.setGiverMember(m2);
        a2.setReceiverMember(m3);
        Assignment a3 = new Assignment();
        a3.setGiverMember(m3);
        a3.setReceiverMember(m1);
        mDatabaseManager.create(a1);
        mDatabaseManager.create(a2);
        mDatabaseManager.create(a3);

        /**
         * Make a second group - testing query doesn't pull these in.
         */

        Group group2 = new GroupBuilder().withName("group2").build();
        mDatabaseManager.create(group2);

        // Add some Members
        Member m1_2 = new MemberBuilder().withName("m1-2").build();
        Member m2_2 = new MemberBuilder().withName("m2-2").build();
        m1_2.setGroup(group2);
        m2_2.setGroup(group2);
        mDatabaseManager.create(m1_2);
        mDatabaseManager.create(m2_2);

        Assignment a1_2 = new Assignment();
        a1_2.setGiverMember(m1_2);
        a1_2.setReceiverMember(m2_2);
        Assignment a2_2 = new Assignment();
        a2_2.setGiverMember(m2_2);
        a2_2.setReceiverMember(m1_2);

        mDatabaseManager.create(a1_2);
        mDatabaseManager.create(a2_2);

        long rowsDeleted = mDatabaseManager.deleteAllAssignmentsForGroup(group1.getId());
        assertEquals(3, rowsDeleted);

        List<Assignment> fromGroup2 = mDatabaseManager.queryAllAssignmentsForGroup(group2.getId());
        assertEquals(2, fromGroup2.size());
    }

    /*
     * Test column constraints
     */

    /**
     * Test table constraints
     */

    public void testCreateGroupNullNameFails() {

        Group g1 = new GroupBuilder().withName(null).build();

        try {
            // Should fails to add Group with null name.
            mDatabaseManager.create(g1);
            fail("Should have thrown SQLException - null Group name is not allowed");
        } catch(SQLException exp) {
            assertTrue(true);
        }
    }

    public void testCreateGroupNotUniqueNameFails() {
        Group g1 = new GroupBuilder().withName("g1").build();
        mDatabaseManager.create(g1);

        // Same name
        Group g2 = new GroupBuilder().withName("g1").build();

        // Now try to get it back (but doesn't exist)
        try {
            mDatabaseManager.create(g2);
            fail("Should have thrown SQLException - non-unique Group name is not allowed");
        } catch(SQLException exp) {
            assertTrue(true);
        }
    }

    /**
     * Verify that the update will not replace another Group with the same name.
     */
    public void testUpdateGroupNotUniqueFails() {

        Group g1 = new GroupBuilder().withName("g1").build();
        Group g2 = new GroupBuilder().withName("g2").build();
        mDatabaseManager.create(g1);
        mDatabaseManager.create(g2);

        // Now try to get it back (but doesn't exist)
        try {
            g2.setName(g1.getName());
            mDatabaseManager.update(g2);
            fail("Should have thrown SQLException - non unique Group name is not allowed");
        } catch(SQLException exp) {
            assertTrue(true);
        }
    }

    public void testCreateGroupNameApostropheOk() {
        // Verify that our queries are safe for names with apostrophes
        Group g1 = new GroupBuilder().withName("Test O'Name").build();
        mDatabaseManager.create(g1);
        assertEquals(g1.getName(), mDatabaseManager.queryById(g1.getId(), Group.class).getName());
    }

    public void testCreateGroupNameQuotesOk() {
        // Verify that our queries are safe for names with quotation marks
        Group g1 = new GroupBuilder().withName("Test O\"Name").build();
        mDatabaseManager.create(g1);
        assertEquals(g1.getName(), mDatabaseManager.queryById(g1.getId(), Group.class).getName());
    }

    public void testDeleteGroupWithMembers() {
        Group g1 = new GroupBuilder().build();
        mDatabaseManager.create(g1);

        // Add some Members
        Member m1 = new MemberBuilder().build();
        m1.setGroup(g1);
        mDatabaseManager.create(m1);

        // Now delete the Group
        mDatabaseManager.delete(g1);

        // Verify that Member should be deleted by cascade
        assertNull(mDatabaseManager.queryById(m1.getId(), Member.class));
    }

    public void testCreateMemberNonUniqueNameDiffGroup() {
        Group g1 = new GroupBuilder().build();
        Group g2 = new GroupBuilder().withName("g2").build();
        mDatabaseManager.create(g1);
        mDatabaseManager.create(g2);

        Member m1 = new MemberBuilder().build();
        m1.setGroup(g1);
        mDatabaseManager.create(m1);

        // Now create another one with the same name BUT for a different Group.
        Member m2 = new MemberBuilder().build();
        try {
            mDatabaseManager.create(m2);
            assertTrue(true); // Should be allowee
        } catch(SQLException exp) {
            fail("Non-unique Member name should be allowed in different Groups");
        }
    }

    public void testCreateMemberNonUniqueNameSameGroupFails() {
        Group g1 = new GroupBuilder().build();
        mDatabaseManager.create(g1);

        Member m1 = new MemberBuilder().build();
        Member m2 = new MemberBuilder().build();
        m1.setGroup(g1);
        m2.setGroup(g1);

        mDatabaseManager.create(m1);

        try {
            // Now create another one with the same name for that group
            mDatabaseManager.create(m2);
            fail("Non-unique Member name should not be allowed in a Group");
        } catch(SQLException exp) {
            assertTrue(true);
        }
    }

    public void testQueryMemberByName() {
        Group g1 = new GroupBuilder().build();
        mDatabaseManager.create(g1);

        Member m1 = new MemberBuilder().build();
        m1.setGroup(g1);
        mDatabaseManager.create(m1);

        // Now fetch the Member by name.
        Member result = mDatabaseManager.queryMemberWithNameForGroup(g1.getId(), m1.getName());

        // Check that it matches what we expect.
        assertNotNull(m1);
        assertEquals(m1.getId(), result.getId());
    }

    public void testDeleteMemberWithRestrictions() {
        // Add a Group
        // Add some Members
        // Add restrictions to member #1
        // Add restrictions to member #2
        // Delete restrictions for member #1.
        // Verify restrictions delete for #1, not #2
        // Add a Group
        Group group1 = new GroupBuilder().build();
        mDatabaseManager.create(group1);

        // Add some Members
        Member m1 = new MemberBuilder().build();
        Member m2 = new MemberBuilder().withName("m2").build();
        Member m3 = new MemberBuilder().withName("m3").build();
        m1.setGroup(group1);
        m2.setGroup(group1);
        m3.setGroup(group1);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);
        mDatabaseManager.create(m3);

        // Add Restrictions
        Restriction m1m2 = new Restriction();
        m1m2.setMember(m1);
        m1m2.setOtherMember(m2);
        mDatabaseManager.create(m1m2);

        Restriction m1m3 = new Restriction();
        m1m2.setMember(m1);
        m1m2.setOtherMember(m3);
        mDatabaseManager.create(m1m3);

        // Delete m1
        mDatabaseManager.delete(m1);

        // Verify restrictions correctly deleted by cascade
        assertEquals(0, mDatabaseManager.queryAllRestrictionsForMemberId(m1.getId()).size());
    }

    public void testQueryHasAssignmentsForGroup() {

        Group group1 = new GroupBuilder().build();
        mDatabaseManager.create(group1);

        boolean beforeMembersResult = mDatabaseManager.queryHasAssignmentsForGroup(group1.getId());
        assertTrue(!beforeMembersResult);

        // Add some Members
        Member m1 = new MemberBuilder().withName("m1").build();
        Member m2 = new MemberBuilder().withName("m2").build();
        Member m3 = new MemberBuilder().withName("m3").build();
        m1.setGroup(group1);
        m2.setGroup(group1);
        m3.setGroup(group1);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);
        mDatabaseManager.create(m3);

        boolean beforeAssigmentsResult = mDatabaseManager.queryHasAssignmentsForGroup(group1.getId());
        assertTrue(!beforeAssigmentsResult);

        Assignment a1 = new Assignment();
        a1.setGiverMember(m1);
        a1.setReceiverMember(m2);
        Assignment a2 = new Assignment();
        a2.setGiverMember(m2);
        a2.setReceiverMember(m3);
        Assignment a3 = new Assignment();
        a3.setGiverMember(m3);
        a3.setReceiverMember(m1);
        mDatabaseManager.create(a1);
        mDatabaseManager.create(a2);
        mDatabaseManager.create(a3);

        boolean afterResult = mDatabaseManager.queryHasAssignmentsForGroup(group1.getId());
        assertTrue(afterResult);
    }

    public void testQueryAssignment() {

        Group group1 = new GroupBuilder().build();
        mDatabaseManager.create(group1);

        // Add some Members
        Member m1 = new MemberBuilder().withName("m1").build();
        Member m2 = new MemberBuilder().withName("m2").build();
        m1.setGroup(group1);
        m2.setGroup(group1);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);

        assertEquals(group1.getId(), m1.getGroupId());
        assertEquals(group1.getId(), m2.getGroupId());

        Assignment a1 = new Assignment();
        a1.setGiverMember(m1);
        a1.setReceiverMember(m2);
        Assignment a2 = new Assignment();
        a2.setGiverMember(m2);
        a2.setReceiverMember(m1);

        mDatabaseManager.create(a1);
        mDatabaseManager.create(a2);

        Assignment a1Query = mDatabaseManager.queryAssignmentForMember(m1.getId());
        Assignment a2Query = mDatabaseManager.queryAssignmentForMember(m2.getId());

        assertEquals(m1.getId(), a1Query.getGiverMemberId());
        assertEquals(m2.getId(), a1Query.getReceiverMemberId());

        assertEquals(m2.getId(), a2Query.getGiverMemberId());
        assertEquals(m1.getId(), a2Query.getReceiverMemberId());
    }

    /*
     * Test the LIKE operator used to find a Group's member when
     * querying a member by name
     */

    // Check case insensitivity
    public void testQueryMemberWithNameCaseInsensitive() {
        // Add a Group
        // Add some members
        // Query and verify
        Group group1 = new GroupBuilder().build();
        mDatabaseManager.create(group1);

        // Add some Members
        Member m1 = new MemberBuilder().withName("m1").build();
        Member m2 = new MemberBuilder().withName("M2").build();
        m1.setGroup(group1);
        m2.setGroup(group1);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);

        Member memberQuery1 = mDatabaseManager.queryMemberWithNameForGroup(group1.getId(), "M1");
        Member memberQuery1_2 = mDatabaseManager.queryMemberWithNameForGroup(group1.getId(), "m1");
        assertEquals("m1", memberQuery1.getName());
        assertEquals("m1", memberQuery1_2.getName());

        Member memberQuery2 = mDatabaseManager.queryMemberWithNameForGroup(group1.getId(), "M2");
        Member memberQuery2_2 = mDatabaseManager.queryMemberWithNameForGroup(group1.getId(), "m2");
        assertEquals("M2", memberQuery2.getName());
        assertEquals("M2", memberQuery2_2.getName());
    }

    // Verify that LIKE operator is only equality, not pattern matching.
    public void testQueryMemberWithNameCaseEqualityOnly() {
        // Add a Group
        // Add some members
        // Query and verify
        Group group1 = new GroupBuilder().build();
        mDatabaseManager.create(group1);

        // Add some Members
        Member m1 = new MemberBuilder().withName("m1").build();
        m1.setGroup(group1);
        mDatabaseManager.create(m1);

        // Various combinations that might pattern match - they shouldn't.
        Member memberQuery1 = mDatabaseManager.queryMemberWithNameForGroup(group1.getId(), "AAM1AA");
        Member memberQuery2 = mDatabaseManager.queryMemberWithNameForGroup(group1.getId(), "AA m1 AA");
        Member memberQuery3 = mDatabaseManager.queryMemberWithNameForGroup(group1.getId(), "AA_m1_AA");
        assertNull(memberQuery1);
        assertNull(memberQuery2);
        assertNull(memberQuery3);
    }

    // Verify query to check for the existence of a Group - when a Group exists
    public void test_hasGroupTrue() {
        Group group1 = new GroupBuilder().build();
        mDatabaseManager.create(group1);
        assertTrue(mDatabaseManager.queryHasGroup());
    }

    // Verify query to check for the existence of a Group - when a Group does not exist
    public void test_hasGroupFalse() {
        assertFalse(mDatabaseManager.queryHasGroup());
    }

    // Verify that the Assignments in a Group can have their SentStatus bulk updated
    // Also verify that this does not impact other Group's Assignments
    public void test_updateAllAssignmentsInGroup() {
        Group group1 = new GroupBuilder().withName("g1").build();
        Group group2 = new GroupBuilder().withName("g2").build();
        mDatabaseManager.create(group1);
        mDatabaseManager.create(group2);

        // Add some Members
        Member m1 = new MemberBuilder().withName("m1").build();
        Member m2 = new MemberBuilder().withName("m2").build();
        Member m3 = new MemberBuilder().withName("m3").build();
        m1.setGroup(group1);
        m2.setGroup(group1);
        m3.setGroup(group1);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);
        mDatabaseManager.create(m3);

        // Attempt with no Assignments
        mDatabaseManager.updateAllAssignmentsInGroup(group1.getId(), Assignment.Status.Assigned);

        // Create the assignments
        Assignment a1 = new Assignment();
        a1.setGiverMember(m1);
        a1.setReceiverMember(m2);
        mDatabaseManager.create(a1);

        Assignment a2 = new Assignment();
        a2.setGiverMember(m2);
        a2.setReceiverMember(m3);
        mDatabaseManager.create(a2);

        Assignment a3 = new Assignment();
        a3.setGiverMember(m3);
        a3.setReceiverMember(m1);
        mDatabaseManager.create(a3);

        /*
         * Create some Members and Assignments for Group2
         * These Assignments should not be affected by the update on Group1
         */
        Member m1_2 = new MemberBuilder().withName("m1_2").build();
        Member m2_2 = new MemberBuilder().withName("m2_2").build();
        Member m3_2 = new MemberBuilder().withName("m3_2").build();
        m1_2.setGroup(group2);
        m2_2.setGroup(group2);
        m3_2.setGroup(group2);
        mDatabaseManager.create(m1_2);
        mDatabaseManager.create(m2_2);
        mDatabaseManager.create(m3_2);

        // Create the assignments
        Assignment a1_2 = new Assignment();
        a1_2.setGiverMember(m1_2);
        a1_2.setReceiverMember(m2_2);
        mDatabaseManager.create(a1_2);

        Assignment a2_2 = new Assignment();
        a2_2.setGiverMember(m2_2);
        a2_2.setReceiverMember(m3_2);
        mDatabaseManager.create(a2_2);

        Assignment a3_2 = new Assignment();
        a3_2.setGiverMember(m3_2);
        a3_2.setReceiverMember(m1_2);
        mDatabaseManager.create(a3_2);

        // Verify initial Assignment state
        assertEquals(Assignment.Status.Assigned, a1.getSendStatus());
        assertEquals(Assignment.Status.Assigned, a2.getSendStatus());
        assertEquals(Assignment.Status.Assigned, a3.getSendStatus());

        assertEquals(Assignment.Status.Assigned, a1_2.getSendStatus());
        assertEquals(Assignment.Status.Assigned, a2_2.getSendStatus());
        assertEquals(Assignment.Status.Assigned, a3_2.getSendStatus());

        // Update all the Assignments for Group1
        mDatabaseManager.updateAllAssignmentsInGroup(group1.getId(), Assignment.Status.Sent);

        // Verify Assignment state updated for Group1
        assertEquals(Assignment.Status.Sent, mDatabaseManager.queryById(a1.getId(), Assignment.class).getSendStatus());
        assertEquals(Assignment.Status.Sent, mDatabaseManager.queryById(a2.getId(), Assignment.class).getSendStatus());
        assertEquals(Assignment.Status.Sent, mDatabaseManager.queryById(a3.getId(), Assignment.class).getSendStatus());

        // Verify Assignment state NOT updated for Group2
        assertEquals(Assignment.Status.Assigned, a1_2.getSendStatus());
        assertEquals(Assignment.Status.Assigned, a2_2.getSendStatus());
        assertEquals(Assignment.Status.Assigned, a3_2.getSendStatus());
    }
}
