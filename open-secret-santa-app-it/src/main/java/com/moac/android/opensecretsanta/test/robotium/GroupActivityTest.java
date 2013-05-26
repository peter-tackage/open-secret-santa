package com.moac.android.opensecretsanta.test.robotium;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;
import android.widget.ImageView;
import com.jayway.android.robotium.solo.Solo;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.activity.DrawTabManagerActivity;
import com.moac.android.opensecretsanta.activity.GroupSelectionActivity;

public class GroupActivityTest extends ActivityInstrumentationTestCase2<GroupSelectionActivity> {

    private Solo solo;

    public GroupActivityTest() {
        super(GroupSelectionActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        //setUp() is run before a test case is started.
        //This is where the solo object is created.
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        //tearDown() is run after a test case has finished.
        //finishOpenedActivities() will finish all the activities that have been opened during the test execution.
        solo.finishOpenedActivities();
    }

    public void testAddGroup() throws Exception {

        // Enter the group name
        EditText groupNameEditText = (EditText) solo.getView(R.id.txtGroupName);

        solo.enterText(groupNameEditText, "Test Group");

        // Goto next activity
        ImageView nextButton = (ImageView) solo.getView(R.id.btnCreateGroup);
        solo.clickOnView(nextButton);

        // Verify that we move to the next screen - the members list.
        solo.assertCurrentActivity("Expected DrawTabManagerActivity", DrawTabManagerActivity.class);

        // Verify that there are no members in the list for this new Group.
        assertEquals(1, solo.getCurrentListViews().size());

        // Return to Group Activity
        solo.goBack();

        // Verify Group with name is in list

        // Verify no members.

//		//Assert that NoteEditor activity is opened
//		solo.assertCurrentActivity("Expected NoteEditor activity", "NoteEditor"); 
//		//In text field 0, add Note 1
//		solo.enterText(0, "Note 1");
//		solo.goBack(); 
//		//Clicks on menu item
//		solo.clickOnMenuItem("Add note");
//		//In text field 0, add Note 2
//		solo.enterText(0, "Note 2");
//		//Go back to first activity named "NotesList"
//		solo.goBackToActivity("NotesList"); 
//		//Takes a screenshot and saves it in "/sdcard/Robotium-Screenshots/".
//		solo.takeScreenshot();
//		boolean expected = true;
//		boolean actual = solo.searchText("Note 1") && solo.searchText("Note 2");
//		//Assert that Note 1 & Note 2 are found
//		assertEquals("Note 1 and/or Note 2 are not found", expected, actual); 

    }

//	public void testEditNote() throws Exception {
//		// Click on the second list line
//		solo.clickInList(2); 
//		// Change orientation of activity
//		solo.setActivityOrientation(Solo.LANDSCAPE);
//		// Change title
//		solo.clickOnMenuItem("Edit title");
//		//In first text field (0), add test
//		solo.enterText(0, " test");  
//		solo.goBack();
//		boolean expected = true;
//		// (Regexp) case insensitive
//		boolean actual = solo.waitForText("(?i).*?note 1 test"); 
//		//Assert that Note 1 test is found
//		assertEquals("Note 1 test is not found", expected, actual); 
//
//	}
//
//	public void testRemoveNote() throws Exception {
//		//(Regexp) case insensitive/text that contains "test"
//		solo.clickOnText("(?i).*?test.*");
//		//Delete Note 1 test
//		solo.clickOnMenuItem("Delete");
//		//Note 1 test & Note 2 should not be found
//		boolean expected = false;   
//		boolean actual = solo.searchText("Note 1 test");
//		//Assert that Note 1 test is not found
//		assertEquals("Note 1 Test is found", expected, actual);  
//		solo.clickLongOnText("Note 2");
//		//Clicks on Delete in the context menu
//		solo.clickOnText("Delete");  
//		actual = solo.searchText("Note 2");
//		//Assert that Note 2 is not found
//		assertEquals("Note 2 is found", expected, actual);  
//	}
}

