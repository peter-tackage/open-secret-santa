package com.moac.android.opensecretsanta.test.database;

import android.database.SQLException;
import android.test.AndroidTestCase;

import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.test.builders.DrawResultBuilder;
import com.moac.android.opensecretsanta.test.builders.DrawResultEntryBuilder;
import com.moac.android.opensecretsanta.test.builders.GroupBuilder;
import com.moac.android.opensecretsanta.test.builders.MemberBuilder;
import com.moac.android.opensecretsanta.types.*;

import java.util.List;

public class DatabaseTests extends AndroidTestCase {

    DatabaseManager mDatabaseManager;
    TestDatabaseHelper mDbHelper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDbHelper = new TestDatabaseHelper(getContext());
        mDatabaseManager = new DatabaseManager(mDbHelper);
        mDbHelper.getWritableDatabase().beginTransaction();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mDbHelper.getWritableDatabase().endTransaction();
        mDatabaseManager = null;
    }

    /*
     * Bespoke Query Tests
     */

    public void testQueryLatestDrawResultForGroup() {
        Group group1 = new GroupBuilder().build();
        mDatabaseManager.create(group1);

        DrawResult dr1 = new DrawResultBuilder().withDrawDate(1).build();
        DrawResult dr2 = new DrawResultBuilder().withDrawDate(2).build();
        DrawResult dr3 = new DrawResultBuilder().withDrawDate(3).build();
        dr1.setGroup(group1);
        dr2.setGroup(group1);
        dr3.setGroup(group1);
        mDatabaseManager.create(dr1);
        mDatabaseManager.create(dr2);
        mDatabaseManager.create(dr3);

        DrawResult dr = mDatabaseManager.queryLatestDrawResultForGroup(group1.getId());
        assertEquals(3, dr.getDrawDate());
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

    /**
     * FIXME This doesn't work at the moment - ORMLIte doesn't enforce the
     * unique constraints on the foreign key reference column. Probably because
     * its required when supporting one-to-many relationships and there's no
     * reasonable way for ORMLite to know what the intended relationship is.
     */
//    public void testCreateSingleDrawResultPerGroupFails() {
//        // Add a Group
//        // Add a Draw Result
//
//        Group group1 = new GroupBuilder().build();
//        mDatabaseManager.create(group1);
//
//        DrawResult dr1 = new DrawResultBuilder().withDrawDate(1).build();
//        DrawResult dr2 = new DrawResultBuilder().withDrawDate(2).build();
//        dr1.setGroup(group1);
//        dr2.setGroup(group1); // <------ same group id!!!
//        mDatabaseManager.create(dr1);
//        try {
//            mDatabaseManager.create(dr2);
//            fail("Only one Draw Result per Group should be allowed");
//        } catch(SQLException exp) {
//            assertTrue(true);
//        }
//    }

    public void testQueryAllDrawResultEntriesForDrawId() {
        // Add a Group
        // Add some Members
        // Add a Draw Result
        // Add some DREs
        // Query for DREs and verify
        Group group1 = new GroupBuilder().build();
        mDatabaseManager.create(group1);

        Member m1 = new MemberBuilder().build();
        Member m2 = new MemberBuilder().build();
        Member m3 = new MemberBuilder().build();
        m1.setGroup(group1);
        m2.setGroup(group1);
        m3.setGroup(group1);
        mDatabaseManager.create(m1);
        mDatabaseManager.create(m2);
        mDatabaseManager.create(m3);

        DrawResult dr1 = new DrawResultBuilder().withDrawDate(1).build();
        dr1.setGroup(group1);
        mDatabaseManager.create(dr1);

        DrawResultEntry dre1 = new DrawResultEntryBuilder().build();
        DrawResultEntry dre2 = new DrawResultEntryBuilder().build();
        DrawResultEntry dre3 = new DrawResultEntryBuilder().build();
        dre1.setDrawResult(dr1);
        dre1.setGiverMember(m1);
        dre1.setReceiverMember(m2);
        dre2.setDrawResult(dr1);
        dre2.setGiverMember(m2);
        dre2.setReceiverMember(m3);
        dre3.setDrawResult(dr1);
        dre3.setGiverMember(m3);
        dre3.setReceiverMember(m1);
        mDatabaseManager.create(dre1);
        mDatabaseManager.create(dre2);
        mDatabaseManager.create(dre3);

        List<DrawResultEntry> dres = mDatabaseManager.queryAllDrawResultEntriesForDrawId(dr1.getId());
        assertEquals(3, dres.size());
    }

    public void testQueryAllDrawResultsForGroup() {
        // Add a Group
        // Add some members
        // Add DrawResult #1
        // Add DrawResult #2
        // Add DrawResult #3
        // Query and verify

        Group group1 = new GroupBuilder().build();
        Group group2 = new GroupBuilder().withName("g2").build();
        mDatabaseManager.create(group1);
        mDatabaseManager.create(group2);

        DrawResult dr1 = new DrawResultBuilder().withDrawDate(1).build();
        DrawResult dr2 = new DrawResultBuilder().withDrawDate(2).build();
        dr1.setGroup(group1);
        dr2.setGroup(group1);
        mDatabaseManager.create(dr1);
        mDatabaseManager.create(dr2);

        List<DrawResult> drs = mDatabaseManager.queryAllDrawResultsForGroup(group1.getId());
        assertEquals(2, drs.size());
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
        } catch(SQLException exp) {
            fail("Non-unique Member name should be allowed in different Groups");
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
}
