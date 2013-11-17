package com.moac.android.opensecretsanta.database;

import android.test.AndroidTestCase;
import com.moac.android.opensecretsanta.model.Assignment;
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
                { Assignment.class, GroupVersion2.class, MemberVersion2.class, DrawResultVersion2.class, DrawResultEntryVersion2.class };

        // TestDatabaseHelper deletes any existing table
        mTestDbHelper = new TestDatabaseHelper(getContext(), TEST_DATABASE_NAME, PERSISTABLE_OBJECTS);
        mTestDbHelper.getWritableDatabase().beginTransaction();

        mDatabaseUpgrader = new DatabaseUpgrader(mTestDbHelper);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        mTestDbHelper.getWritableDatabase().endTransaction();
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

    public void testRemoveGroupVersion2() {
        // add groupA, groupB, groupC
        GroupVersion2 groupA = new GroupVersion2();
        groupA.setName("gA");
        groupA.setReady(true);
        long groupAId = mTestDbHelper.create(groupA, GroupVersion2.class);

        GroupVersion2 groupB = new GroupVersion2();
        groupB.setName("gB");
        groupB.setReady(false);
        long groupBId = mTestDbHelper.create(groupB, GroupVersion2.class);

        GroupVersion2 groupC = new GroupVersion2();
        groupC.setName("gC");
        groupC.setReady(false);
        long groupCId =  mTestDbHelper.create(groupC, GroupVersion2.class);

        // remove group groupB
        mDatabaseUpgrader.removeGroupVersion2(groupBId);
        List<GroupVersion2> groupsTestResults =  mTestDbHelper.queryAll(GroupVersion2.class);

        // test that groupA and groupC remain
        assertEquals(2, groupsTestResults.size());
        assertTrue(groupsTestResults.contains(groupA));
        assertTrue(groupsTestResults.contains(groupC));
    }
}
