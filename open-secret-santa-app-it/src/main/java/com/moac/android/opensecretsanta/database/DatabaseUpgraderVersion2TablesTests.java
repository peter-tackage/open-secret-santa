package com.moac.android.opensecretsanta.database;

import android.test.AndroidTestCase;
import com.moac.android.opensecretsanta.builders.MemberVersion2Builder;
import com.moac.android.opensecretsanta.model.ContactMethod;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.version2.DrawResultEntryVersion2;
import com.moac.android.opensecretsanta.model.version2.DrawResultVersion2;
import com.moac.android.opensecretsanta.model.version2.GroupVersion2;
import com.moac.android.opensecretsanta.model.version2.MemberVersion2;

import java.util.List;

public class DatabaseUpgraderVersion2TablesTests extends AndroidTestCase {

    private static final String TEST_DATABASE_NAME = "testopensecretsanta.db";

    TestDatabaseHelper mTestDbHelper;
    DatabaseUpgrader mDatabaseUpgrader;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Class[] PERSISTABLE_OBJECTS = new Class[]
                { GroupVersion2.class, MemberVersion2.class, DrawResultVersion2.class, DrawResultEntryVersion2.class };

        // TestDatabaseHelper deletes any existing table
        mTestDbHelper = new TestDatabaseHelper(getContext(), TEST_DATABASE_NAME, PERSISTABLE_OBJECTS);
        mTestDbHelper.getWritableDatabase().beginTransaction();

        mDatabaseUpgrader = new DatabaseUpgrader(mTestDbHelper);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        mTestDbHelper.getWritableDatabase().endTransaction();
        getContext().deleteDatabase("/data/data/com.moac.android.opensecretsanta/databases/" + TEST_DATABASE_NAME);
        mDatabaseUpgrader = null;
    }

    public void testGetAllDrawResultsVersion2ForGroupWithDrawResult() {

        // Add a group that has a draw result
        GroupVersion2 groupA = new GroupVersion2();
        groupA.setName("gA");
        groupA.setReady(true);
        mTestDbHelper.create(groupA, GroupVersion2.class);

        DrawResultVersion2 drawResultOne = new DrawResultVersion2();
        drawResultOne.setGroup(groupA);
        mTestDbHelper.create(drawResultOne, DrawResultVersion2.class);

        DrawResultVersion2 drawResultTwo = new DrawResultVersion2();
        drawResultTwo.setGroup(groupA);
        mTestDbHelper.create(drawResultTwo, DrawResultVersion2.class);

        List<DrawResultVersion2> testResults = mDatabaseUpgrader.getAllDrawResultsVersion2ForGroup(groupA.getId());

        assertEquals(2, testResults.size());
        assertTrue(testResults.contains(drawResultOne));
        assertTrue(testResults.contains(drawResultTwo));
    }

    public void testGetAllDrawResultsVersion2ForGroupWithNoDrawResult() {

        // Add a group that doesn't have a draw result
        GroupVersion2 groupB = new GroupVersion2();
        groupB.setName("gB");
        groupB.setReady(false);
        mTestDbHelper.create(groupB, GroupVersion2.class);

        List<DrawResultVersion2> testResults = mDatabaseUpgrader.getAllDrawResultsVersion2ForGroup(groupB.getId());
        assertEquals(0, testResults.size());
    }

    public void testConvertMemberToVersion3() {
        Group.GroupBuilder groupBuilder = new Group.GroupBuilder();
        MemberVersion2Builder memberBuilder = new MemberVersion2Builder();

        MemberVersion2 memberNameOnly = memberBuilder.withContactMode(0).build();
        MemberVersion2 memberSMS = memberBuilder.withContactMode(1).build();
        MemberVersion2 memberEmail = memberBuilder.withContactMode(2).build();

        Member.MemberBuilder memberVersion3Builder = new Member.MemberBuilder();

        Member expectedMemberNameOnly =  memberVersion3Builder.withContactMethod(ContactMethod.REVEAL_ONLY)
                .withName(MemberVersion2Builder.TEST_MEMBER_NAME)
                .withContactDetails(MemberVersion2Builder.TEST_MEMBER_CONTACT_DETAILS)
                .withLookupKey(MemberVersion2Builder.TEST_MEMBER_LOOKUP_KEY)
                .withGroup(groupBuilder.withGroupId(memberNameOnly.getGroupId()).build())
                .build();
        assertEquals(expectedMemberNameOnly, mDatabaseUpgrader.convertMemberToVersion3(memberNameOnly));

    }
}
