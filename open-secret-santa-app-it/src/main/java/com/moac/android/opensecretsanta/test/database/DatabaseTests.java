package com.moac.android.opensecretsanta.test.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.test.AndroidTestCase;
import android.util.Log;
import com.moac.android.opensecretsanta.database.OpenSecretSantaDB;
import com.moac.android.opensecretsanta.test.builders.DrawResultBuilder;
import com.moac.android.opensecretsanta.test.builders.GroupBuilder;
import com.moac.android.opensecretsanta.test.builders.MemberBuilder;
import com.moac.android.opensecretsanta.types.DrawResult;
import com.moac.android.opensecretsanta.types.Group;
import com.moac.android.opensecretsanta.types.Member;
import com.moac.android.opensecretsanta.types.Member.RestrictionsColumns;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class DatabaseTests extends AndroidTestCase {

    OpenSecretSantaDB testDB;

    @Override
    public Context getContext() {
        return super.getContext();
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testDB = new OpenSecretSantaDB(getContext());
        testDB.getDBImpl().beginTransaction();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        testDB.getDBImpl().endTransaction();
        testDB = null;
    }

    public void testGroupCreateIdAssignment() {
        Group g1 = new GroupBuilder().build();
        long id = testDB.insertGroup(g1);

        // ID should be valid
        assertTrue(id > -1);
    }

    public void testGroupCreateName() {

        Group g1 = new GroupBuilder().build();
        long id = testDB.insertGroup(g1);

        // Now get it back from DB.
        Group result = testDB.getGroupById(id);

        // Verify returned name equals name in inserted object.
        assertEquals(g1.getName(), result.getName());
        assertEquals(id, result.getId());
    }

    public void testGroupCreateFailureDoesNotInsert() {

        // Now try to get it back (but doesn't exist)
        try {
            Group result = testDB.getGroupById(-1);
            fail("Should have thrown SQLException");
        } catch(SQLException exp) {
            assertTrue(true);
        }
    }

    public void testGroupCreateNullNameFails() {

        Group g1 = new GroupBuilder().withName(null).build();
        long id = testDB.insertGroup(g1);

        // Fails to add.
        assertEquals(-1, id);

        // Now try to get it back (but doesn't exist)
        try {
            Group result = testDB.getGroupById(id);
            fail("Should have thrown SQLException");
        } catch(SQLException exp) {
            assertTrue(true);
        }
    }

    public void testGroupCreateNotUnique() {

        Group g1 = new GroupBuilder().build();
        long gid1 = testDB.insertGroup(g1);

        Group g2 = new GroupBuilder().build();
        long gid2 = testDB.insertGroup(g2);

        // should fail to add.
        assertEquals(-1, gid2);
    }

    public void testGroupReadAll() {
        Log.v("#####", "######## PRE: " + testDB.getAllGroups().size());

        Group g1 = new GroupBuilder().build();
        long id = testDB.insertGroup(g1);

        Group g2 = new GroupBuilder().withName("Group2").build();
        long id2 = testDB.insertGroup(g2);

        Group g3 = new GroupBuilder().withName("Group3").build();
        long id3 = testDB.insertGroup(g3);

        // ID should be valid
        List<Group> groups = testDB.getAllGroups();

        // Just do this for now
        // TODO Verify unique contents
        assertEquals(3, groups.size());
    }

    /**
     * Verify that the update will not replace another Group with the same name.
     */
    public void testGroupUpdateNotUnique() {

        // Create an initial Group
        Group g1 = new GroupBuilder().build();
        long gid1 = testDB.insertGroup(g1);

        // Create a second Group (with a different name)
        final String name2 = "A different name";
        Group g2 = new GroupBuilder().withName(name2).build();
        long gid2 = testDB.insertGroup(g2);

        // Modify g2 to have the same name as g1
        // => Should disallow.
        try {
            g2.setName(g1.getName());
            testDB.updateGroup(gid2, g2);
            fail("Should have thrown SQLiteConstraintException due to non-unique name.");
        } catch(SQLiteConstraintException ex) {
            Group newG2 = testDB.getGroupById(gid2);
            assertEquals(name2, newG2.getName()); // Verify name is not changed.
        }
    }

    public void testGroupCreateApostrophe() {
        Group g1 = new GroupBuilder().withName("Test O'Name").build();
        long gid = testDB.insertGroup(g1);
    }

    public void testGroupCreateQuotes() {
        Group g1 = new GroupBuilder().withName("Test O\"Name").build();
        long gid = testDB.insertGroup(g1);
    }

    public void testGroupUpdateBasic() {

        Group g1 = new GroupBuilder().build();
        long row = testDB.insertGroup(g1);

        // Now modify the draw
        final String nameUpdate = "Test Group UPDATED";
        g1.setName(nameUpdate);

        assertTrue(testDB.updateGroup(row, g1));

        // Now get it back.
        Group result = testDB.getGroupById(row);
        assertEquals(nameUpdate, result.getName());
    }

    /**
     * Can't update a name to be NULL
     */
    public void testGroupUpdateNullNameFails() {
        Group g1 = new GroupBuilder().build();
        long id = testDB.insertGroup(g1);

        // Now change the name to be NULL
        g1.setName(null);

        try {
            // Verify failed to update
            testDB.updateGroup(id, g1);
            fail("Should have thrown SQLiteConstraintException due to NULL key");
        } catch(SQLiteConstraintException ex) {
            assertTrue(true);
        }
    }

    public void testGroupDeleteBasic() {
        Group g1 = new GroupBuilder().build();

        long g1id = testDB.insertGroup(g1);
        assertTrue(testDB.removeGroup(g1id));
    }

    /**
     * TODO is this doubling up?
     */
    public void testGroupDeleteNoSuchGroupFail() {
        // Should fail when deleting non-existent Group.
        try {

            Group unused = testDB.getGroupById(10);
            fail("Should have thrown SQLException");
        } catch(SQLException ex) {
            assertTrue(true);
        }
    }

    public void testGroupDeleteWithMembers() {
        Group g1 = new GroupBuilder().build();

        // Testing that participants are removed.
        Member m1 = new MemberBuilder().withName("John").build();
        Member m2 = new MemberBuilder().withName("Matt").build();

        long gid = testDB.insertGroup(g1);
        long m1id = testDB.insertMember(gid, m1);
        long m2id = testDB.insertMember(gid, m2);

        assertTrue(testDB.removeGroup(gid));
        int countAfter = testDB.getAllMembers(gid).size();
        assertEquals(0, countAfter);
    }

    /**
     * TODO These "Create" tests are testing WRITE and READ.
     */
    public void testMemberCreateBasic() {

        Group g1 = new GroupBuilder().build();
        long gid = testDB.insertGroup(g1);

        Member m1 = new MemberBuilder().build();
        long mid = testDB.insertMember(gid, m1);

        // Now get it back.
        Member result = testDB.getMemberById(mid);

        // Verify its contents.
        assertEquals(mid, result.getId());
        assertEquals(m1.getName(), result.getName());
        assertEquals(m1.getContactDetail(), result.getContactDetail());
        assertEquals(m1.getContactMode(), result.getContactMode());
        assertEquals(m1.getLookupKey(), result.getLookupKey());
    }

    public void testMemberCreateApostrophe() {

        Group g1 = new GroupBuilder().build();
        long gid = testDB.insertGroup(g1);

        Member m1 = new MemberBuilder().withName("Dan O'Connell").build();
        long mid = testDB.insertMember(gid, m1);

        // Now get it back.
        Member result = testDB.getMemberById(mid);
        assertEquals(m1.getName(), result.getName());
    }

    public void testMemberReadByIdFailureDoesNotExist() {

        // Attempt to retrieve a nonexistent member
        try {
            testDB.getMemberById(4);
            fail("Should have thrown SQLException");
        } catch(SQLException ex) {
            assertTrue(true);
        }
    }

    public void testMemberReadByName() {

        Group g1 = new GroupBuilder().build();
        long gid = testDB.insertGroup(g1);

        Group g2 = new GroupBuilder().withName("Group2").build();
        long gid2 = testDB.insertGroup(g2);

        Member m1 = new MemberBuilder().build();
        long mid = testDB.insertMember(gid, m1);

        // Now create another one with the same name for a different Group.
        // Give a difference so we can tell
        Member m2 = new MemberBuilder().withContactDetail("anotheremail").build();
        long mid2 = testDB.insertMember(gid2, m2);

        // Now fetch the first one.
        Member result = testDB.getMember(gid, m1.getName());

        // Check that it matches what we expect.
        assertEquals(mid, result.getId());
        assertEquals(m1.getContactDetail(), result.getContactDetail());
        assertEquals(m1.getContactMode(), result.getContactMode());
        assertEquals(m1.getName(), result.getName());
        assertEquals(m1.getLookupKey(), result.getLookupKey());
        assertFalse(m2.getContactDetail().equals(m1.getContactDetail()));
    }

    public void testMemberReadByNameFailureDoesNotExist() {

        Group g1 = new GroupBuilder().build();
        long gid = testDB.insertGroup(g1);

        Member m1 = new MemberBuilder().build();
        long mid = testDB.insertMember(gid, m1);

        // Now fetch by name - should be null as it doesn't exist.
        assertNull(testDB.getMember(mid, "Namedoesntexist"));
    }

    public void testMemberReadAll() {
        Group g1 = new GroupBuilder().build();
        long gid = testDB.insertGroup(g1);

        Member m1 = new MemberBuilder().build();
        long mid = testDB.insertMember(gid, m1);
        Member m2 = new MemberBuilder().withName("Name2").build();
        long mid2 = testDB.insertMember(gid, m2);
        Member m3 = new MemberBuilder().withName("Name3").build();
        long mid3 = testDB.insertMember(gid, m3);

        // Now get it back.
        Map<String, Member> result = testDB.getAllMembers(gid);
        assertEquals(3, result.size());
        assertTrue(result.containsKey(m1.getName()));
        assertTrue(result.containsKey(m2.getName()));
        assertTrue(result.containsKey(m3.getName()));
    }

    public void testMemberReadAllCursor() {
        Group g1 = new GroupBuilder().build();
        long gid = testDB.insertGroup(g1);

        Member m1 = new MemberBuilder().build();
        long mid = testDB.insertMember(gid, m1);
        Member m2 = new MemberBuilder().withName("Name2").build();
        long mid2 = testDB.insertMember(gid, m1);
        Member m3 = new MemberBuilder().withName("Name3").build();
        long mid3 = testDB.insertMember(gid, m1);

        // Now get it back.
        Cursor result = testDB.getAllMembersCursor(gid);
        assertEquals(3, result.getCount());
    }

    public void testMemberUpdate() {

        Group g1 = new GroupBuilder().build();
        Member m1 = new MemberBuilder().build();

        long gid = testDB.insertGroup(g1);
        long mid = testDB.insertMember(gid, m1);

        // Now modify it.
        m1.setName("User 2");
        m1.setContactDetail("5678");
        m1.setContactMode(2);
        m1.setLookupKey("AAAABBBB");

        // Update in DB
        boolean updateResult = testDB.updateMember(mid, m1);

        assertTrue(updateResult);

        // Verify update.
        Member returnedMember = testDB.getMemberById(mid);

        assertEquals(m1.getName(), returnedMember.getName());
        assertEquals(m1.getContactDetail(), returnedMember.getContactDetail());
        assertEquals(m1.getContactMode(), returnedMember.getContactMode());
        assertEquals(m1.getLookupKey(), returnedMember.getLookupKey());
    }

    public void testMemberUpdateNoSuchMemberFail() {
        // Create object with a valid id, but no such member in DB.
        Member m1 = new MemberBuilder().withId(1).build();

        // Should return false when update fails.
        assertFalse(testDB.updateMember(m1.getId(), m1));
    }

	/*
	 * The setMembers method has been removed from the application code
	 * as it was not being used.
	 */

    //	// Test set the members of a group to a specific set.
//	// Test modifying the set to Create, Delete and Update
//	// in the one motion.
//	public void testMemberSetMembers()
//	{
//		Group g1 = new GroupBuilder().build();
//		long gid = testDB.insertGroup(g1);
//		
//		Member m1 = new MemberBuilder().build();
//		long mid = testDB.insertMember(gid, m1);
//		Member m2 = new MemberBuilder().withName("Name2").build();
//		long mid2 = testDB.insertMember(gid, m1);
//		Member m3 = new MemberBuilder().withName("Name3").build();
//		long mid3 = testDB.insertMember(gid, m1);
//		
//		// Now fetch back, populated.
//		m1 = testDB.getMemberById(mid);
//		m2 = testDB.getMemberById(mid2);
//		m3 = testDB.getMemberById(mid3);
//
//		// So we will
//		// m1 - delete
//		// m2 - update
//		// m3 - same
//		
//		// And add m4.
//		
//		HashMap<String, Member> members = new HashMap<String, Member>();
//		m2.setName("New Name2");
//		members.put(m2.getName(), m2);
//		members.put(m3.getName(), m3);
//		
//		Member m4 = new MemberBuilder().withName("Name4").build();
//		
//		testDB.setMembers(gid, members);
//
//	}
//	
//	public void testMemberSetMembersFailNotSuchGroup()
//	{
//		// Should return false because the Group doesn't exist.
//		assertFalse(testDB.setMembers(1,new HashMap<String, Member>()));
//	}
//	
    public void testMemberSetMembersFailMixedGroup() {

    }

    public void testMemberDeleteBasic() {
        Group g1 = new GroupBuilder().build();
        long gid = testDB.insertGroup(g1);

        Member m1 = new MemberBuilder().build();
        long mid = testDB.insertMember(gid, m1);

        // Now get remove it
        assertTrue(testDB.removeMember(mid));

        // Verify that it's not there.
        try {
            Member m1returned = testDB.getMemberById(mid);
            fail("Should have thrown SQLException as Member doesn't exist");
        } catch(SQLException ex) {
            assertTrue(true);
        }
    }

    public void testMemberDeleteNoSuchMemberFail() {
        // Create object with a valid id, but no such member in DB.
        Member m1 = new MemberBuilder().withId(1).build();

        // Should return false when delete fails.
        assertFalse(testDB.removeMember(m1.getId()));
    }

    /**
     * Create/Read Restriction
     */
    public void testRestrictionCreateBasic() {
        Group g1 = new GroupBuilder().build();
        long gid = testDB.insertGroup(g1);

        Member m1 = new MemberBuilder().withName("member1").build();
        long mid1 = testDB.insertMember(gid, m1);

        Member m2 = new MemberBuilder().withName("member2").build();
        long mid2 = testDB.insertMember(gid, m2);

        // Now define some restrictions
        long rid = testDB.insertRestriction(mid1, mid2);

        assertTrue(rid != -1);

        // Now verify the READ
        Cursor cursor = testDB.getRestrictionByIdCursor(rid);
        while(cursor.moveToNext()) {
            assertEquals(mid1, cursor.getLong((cursor.getColumnIndex(RestrictionsColumns.MEMBER_ID_COLUMN))));
            assertEquals(mid2, cursor.getLong((cursor.getColumnIndex(RestrictionsColumns.OTHER_MEMBER_ID_COLUMN))));
        }
        cursor.close();
    }

    public void testRestrictionDeleteBasic() {
        Group g1 = new GroupBuilder().build();
        long gid = testDB.insertGroup(g1);

        Member m1 = new MemberBuilder().withName("Member 1").build();
        long mid1 = testDB.insertMember(gid, m1);

        Member m2 = new MemberBuilder().withName("Member 2").build();
        long mid2 = testDB.insertMember(gid, m2);

        // Now define a restriction m1 /=> m2
        long rid = testDB.insertRestriction(mid1, mid2);

        // Now delete that restriction.
        assertTrue(testDB.removeRestriction(rid));

        // Now verify that it doesn't exist.
        try {
            testDB.getRestrictionByIdCursor(rid);
            fail("Should have raise SQLException as Restriction has been removed");
        } catch(SQLException exp) {
            assertTrue(true);
        }
    }

    /*
     * Verify that restrictions involving with a member are deleted
     * when that member is deleted.
     */
    public void testMemberDeleteWithRestrictions() {

        // Define the Group
        Group g1 = new GroupBuilder().build();
        long gid = testDB.insertGroup(g1);

        // Define the members
        Member m1 = new MemberBuilder().withName("John").build();
        Member m2 = new MemberBuilder().withName("Matt").build();
        Member m3 = new MemberBuilder().withName("Maud").build();

        long mid1 = testDB.insertMember(gid, m1);
        long mid2 = testDB.insertMember(gid, m2);
        long mid3 = testDB.insertMember(gid, m3);

        // Now define some restrictions
        // 2 /=> 3
        // 2 /=> 1
        // 3 /=> 2
        long rid1 = testDB.insertRestriction(mid2, mid1); // from m2
        long rid2 = testDB.insertRestriction(mid3, mid2); // to m2

        // Now delete m2
        assertTrue(testDB.removeMember(mid2));

        // Verify that no restrictions exist involving m2.
        // So rid1 and rid2 should be deleted.

        Cursor cursor = null;
        try {
            cursor = testDB.getRestrictionByIdCursor(rid1);
            fail("Should have raised SQLException as should deleted");
        } catch(SQLException exp) {
            assertTrue(true);
        }
        try {
            cursor = testDB.getRestrictionByIdCursor(rid2);
            fail("Should have raised SQLException as should deleted");
        } catch(SQLException exp) {
            assertTrue(true);
        }
    }

    // TODO Don't delete unaffected restrictions

    /*
     * DrawResult Create/Read
     */
    public void testDrawResultCreateBasic() {

        // Create parent Group
        Group g1 = new GroupBuilder().build();
        long gid = testDB.insertGroup(g1);

        // Create DrawResult
        DrawResult dr1 = new DrawResultBuilder().build();
        long drId = testDB.insertDrawResult(dr1, gid);

        // Read DrawResult
        DrawResult drReturned = testDB.getDrawResultById(drId);

        // Verify the DrawResult matches that Created.
        assertFalse(drReturned.getId() == -1);
        assertEquals(dr1.getDrawDate(), drReturned.getDrawDate());
        assertEquals(dr1.getSendDate(), drReturned.getSendDate());
        assertEquals(dr1.getMessage(), drReturned.getMessage());
    }

    public void testDrawResultUpdate() {

        Group g1 = new GroupBuilder().build();
        long gid = testDB.insertGroup(g1);

        DrawResult dr1 = new DrawResultBuilder().build();
        long drId = testDB.insertDrawResult(dr1, gid);

        // Now modify draw date, send date and message.
        long dateMod = Date.parse("4 August, 2011");
        dr1.setDrawDate(dateMod);
        long dateMod2 = Date.parse("5 August, 2011");
        dr1.setSendDate(dateMod2);
        dr1.setMessage("New Message");

        assertTrue(testDB.updateDrawResult(dr1, drId));

        // Now retrieve and check the Draw Date has been updated.
        DrawResult drReturned = testDB.getDrawResultById(drId);
        assertEquals(dr1.getDrawDate(), drReturned.getDrawDate());
        assertEquals(dr1.getSendDate(), drReturned.getSendDate());
        assertEquals(dr1.getMessage(), drReturned.getMessage());
    }

    public void testDrawResultDelete() {
        Group g1 = new GroupBuilder().build();
        long gid = testDB.insertGroup(g1);

        DrawResult dr1 = new DrawResultBuilder().build();
        long drId = testDB.insertDrawResult(dr1, gid);

        // Now get remove it
        assertTrue(testDB.removeDrawResult(drId));

        // Verify that it's not there.
        try {
            DrawResult dr1returned = testDB.getDrawResultById(drId);
            fail("Should have thrown SQLException as Member doesn't exist");
        } catch(SQLException ex) {
            assertTrue(true);
        }
    }
}
