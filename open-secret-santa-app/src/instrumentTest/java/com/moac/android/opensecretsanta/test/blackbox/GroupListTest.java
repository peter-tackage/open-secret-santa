package com.moac.android.opensecretsanta.test.blackbox;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import com.jayway.android.robotium.solo.Solo;
import com.moac.android.opensecretsanta.test.DateTestUtils;

/**
 * Blackbox tests for the Group List drawer
 *
 * As this is a blackbox test, we cannot have any application code dependencies
 */
public class GroupListTest extends ActivityInstrumentationTestCase2<Activity> {

    private static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME =
            "com.moac.android.opensecretsanta.activity.MainActivity";

    private static Class launcherActivityClass;

    static {
        try {
            launcherActivityClass = Class
                    .forName(LAUNCHER_ACTIVITY_FULL_CLASSNAME);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public GroupListTest() throws ClassNotFoundException {
        super(launcherActivityClass);
    }

    private Solo solo;

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    /**
     * Verify the Group List contains a default created Group
     */
    public void testDefaultInitialGroupCreated() throws Exception {

        // Check that we have the right activity under test
        solo.assertCurrentActivity("Incorrect Activity", launcherActivityClass);

        // Open sliding drawer using application button
        solo.clickOnActionBarHomeButton();

        // Wait for the Drawer to open and the header text to be visible
        solo.waitForText("My Groups");

        // Verify we have an Add Group "button"
        assertTrue(solo.searchText("Add Group"));

        // Get the approximate creation time of the Group
        long now = System.currentTimeMillis();

        // Get the DrawerLayout list (it's the second list)
        ListView list = solo.getCurrentViews(ListView.class).get(1);

        // Verify we have a header, but no footer item.
        assertEquals(1, list.getHeaderViewsCount());
        assertEquals(0, list.getFooterViewsCount());

        // Verify length includes Header, Button and Default Item
        assertEquals(3, list.getCount());

        // Verify the default item is checked
        assertEquals(2, list.getCheckedItemPosition());

        // Verify the contents of the default Group
        assertTrue(solo.searchText("My Group " + DateTestUtils.getDate("yyyy", now) + " #1"));
        assertTrue(solo.searchText(DateTestUtils.getDate("d MMM yyyy", now)));

    }
}
