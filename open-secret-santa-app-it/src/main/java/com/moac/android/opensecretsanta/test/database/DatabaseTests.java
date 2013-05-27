package com.moac.android.opensecretsanta.test.database;


import android.database.SQLException;
import android.test.AndroidTestCase;

import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.test.builders.GroupBuilder;
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

        // Add a Group
        // Add some members
        // Add DrawResult #1
        // Add DrawResult #2
        // Add DrawResult #3
        // Query and verify is #3
    }

    public void testQueryAllRestrictionsForMemberId() {
        // Add a Group
        // Add some Members
        // Add restrictions to member #1
        // Add restrictions to member #2
        // Query verify all restrictions returned
        // Query restrictions for member #1.  verify restrictions returned

    }

    public void testQueryAllDrawResultEntriesForDrawId() {
        // Add a Group
        // Add some Members
        // Add a Draw Result
        // Add some DREs
        // Query for DREs and verify
    }

    public void testQueryAllDrawResultsForGroup() {
        // Add a Group
        // Add some members
        // Add DrawResult #1
        // Add DrawResult #2
        // Add DrawResult #3
        // Query and verify
    }

    public void testQueryAllMembersForGroup() {
        // Add a Group
        // Add some members
        // Query and verify
    }

    public void testQueryAllMembersForGroupExcept() {
        // Add a Group
        // Add some members
        // Query and verify
    }

    public void testQueryMemberWithNameForGroup() {
        // Add a Group
        // Add some members
        // Query and verify
    }

    public void testDeleteAllRestrictionsForMember() {
        // Add a Group
        // Add some Members
        // Add restrictions to member #1
        // Add restrictions to member #2
        // Delete restrictions for member #1.
        // Verify restrictions delete for #1, not #2
    }

    /*
     * Test column constraints
     */

    /**
     * Test table constraints
     */

    public void testGroupCreateNullNameFails() {

        Group g1 = new GroupBuilder().withName(null).build();
        long id = mDatabaseManager.create(g1);

        // Fails to add.
        assertEquals(PersistableObject.UNSET_ID, id);

        // Now try to get it back (but doesn't exist)
        try {
            Group result = mDatabaseManager.queryById(id, Group.class);
            fail("Should have thrown SQLException");
        } catch(SQLException exp) {
            assertTrue(true);
        }
    }

    public void testGroupCreateNotUnique() {

    }

    /**
     * Verify that the update will not replace another Group with the same name.
     */
    public void testGroupUpdateNotUnique() {

//        // Create an initial Group
//        Group g1 = new GroupBuilder().build();
//        long gid1 = testDB.insertGroup(g1);
//
//        // Create a second Group (with a different name)
//        final String name2 = "A different name";
//        Group g2 = new GroupBuilder().withName(name2).build();
//        long gid2 = testDB.insertGroup(g2);
//
//        // Modify g2 to have the same name as g1
//        // => Should disallow.
//        try {
//            g2.setName(g1.getName());
//            testDB.updateGroup(gid2, g2);
//            fail("Should have thrown SQLiteConstraintException due to non-unique name.");
//        } catch(SQLiteConstraintException ex) {
//            Group newG2 = testDB.getGroupById(gid2);
//            assertEquals(name2, newG2.getName()); // Verify name is not changed.
//        }
    }

    public void testGroupCreateApostrophe() {
  //      Group g1 = new GroupBuilder().withName("Test O'Name").build();
  //      long gid = testDB.insertGroup(g1);
    }

    public void testGroupCreateQuotes() {
  //      Group g1 = new GroupBuilder().withName("Test O\"Name").build();
  //      long gid = testDB.insertGroup(g1);
    }


    /**
     * Can't update a name to be NULL
     */
    public void testGroupUpdateNullNameFails() {
//        Group g1 = new GroupBuilder().build();
//        long id = testDB.insertGroup(g1);
//
//        // Now change the name to be NULL
//        g1.setName(null);
//
//        try {
//            // Verify failed to update
//            testDB.updateGroup(id, g1);
//            fail("Should have thrown SQLiteConstraintException due to NULL key");
//        } catch(SQLiteConstraintException ex) {
//            assertTrue(true);
//        }
    }


    public void testGroupDeleteWithMembers() {
//        Group g1 = new GroupBuilder().build();
//
//        // Testing that participants are removed.
//        Member m1 = new MemberBuilder().withName("John").build();
//        Member m2 = new MemberBuilder().withName("Matt").build();
//
//        long gid = testDB.insertGroup(g1);
//        long m1id = testDB.insertMember(gid, m1);
//        long m2id = testDB.insertMember(gid, m2);
//
//        assertTrue(testDB.removeGroup(gid));
//        int countAfter = testDB.getAllMembers(gid).size();
//        assertEquals(0, countAfter);
    }


    public void testMemberCreateApostrophe() {
//        Group g1 = new GroupBuilder().build();
//        long gid = testDB.insertGroup(g1);
//
//        Member m1 = new MemberBuilder().withName("Dan O'Connell").build();
//        long mid = testDB.insertMember(gid, m1);
//
//        // Now get it back.
//        Member result = testDB.getMemberById(mid);
//        assertEquals(m1.getName(), result.getName());
    }


    public void testMemberReadByName() {

//        Group g1 = new GroupBuilder().build();
//        long gid = testDB.insertGroup(g1);
//
//        Group g2 = new GroupBuilder().withName("Group2").build();
//        long gid2 = testDB.insertGroup(g2);
//
//        Member m1 = new MemberBuilder().build();
//        long mid = testDB.insertMember(gid, m1);
//
//        // Now create another one with the same name for a different Group.
//        // Give a difference so we can tell
//        Member m2 = new MemberBuilder().withContactDetail("anotheremail").build();
//        long mid2 = testDB.insertMember(gid2, m2);
//
//        // Now fetch the first one.
//        Member result = testDB.getMember(gid, m1.getName());
//
//        // Check that it matches what we expect.
//        assertEquals(mid, result.getId());
//        assertEquals(m1.getContactDetail(), result.getContactDetail());
//        assertEquals(m1.getContactMode(), result.getContactMode());
//        assertEquals(m1.getName(), result.getName());
//        assertEquals(m1.getLookupKey(), result.getLookupKey());
//        assertFalse(m2.getContactDetail().equals(m1.getContactDetail()));
    }

    public void testMemberReadByNameFailureDoesNotExist() {
//
//        Group g1 = new GroupBuilder().build();
//        long gid = testDB.insertGroup(g1);
//
//        Member m1 = new MemberBuilder().build();
//        long mid = testDB.insertMember(gid, m1);
//
//        // Now fetch by name - should be null as it doesn't exist.
//        assertNull(testDB.getMember(mid, "Namedoesntexist"));
    }


    /*
     * Verify that restrictions involving with a member are deleted
     * when that member is deleted.
     */
    public void testMemberDeleteWithRestrictions() {

//        // Define the Group
//        Group g1 = new GroupBuilder().build();
//        long gid = testDB.insertGroup(g1);
//
//        // Define the members
//        Member m1 = new MemberBuilder().withName("John").build();
//        Member m2 = new MemberBuilder().withName("Matt").build();
//        Member m3 = new MemberBuilder().withName("Maud").build();
//
//        long mid1 = testDB.insertMember(gid, m1);
//        long mid2 = testDB.insertMember(gid, m2);
//        long mid3 = testDB.insertMember(gid, m3);
//
//        // Now define some restrictions
//        // 2 /=> 3
//        // 2 /=> 1
//        // 3 /=> 2
//        long rid1 = testDB.insertRestriction(mid2, mid1); // from m2
//        long rid2 = testDB.insertRestriction(mid3, mid2); // to m2
//
//        // Now delete m2
//        assertTrue(testDB.removeMember(mid2));
//
//        // Verify that no restrictions exist involving m2.
//        // So rid1 and rid2 should be deleted.
//
//        Cursor cursor = null;
//        try {
//            cursor = testDB.getRestrictionByIdCursor(rid1);
//            fail("Should have raised SQLException as should deleted");
//        } catch(SQLException exp) {
//            assertTrue(true);
//        }
//        try {
//            cursor = testDB.getRestrictionByIdCursor(rid2);
//            fail("Should have raised SQLException as should deleted");
//        } catch(SQLException exp) {
//            assertTrue(true);
//        }
    }

    // TODO Don't delete unaffected restrictions

}
