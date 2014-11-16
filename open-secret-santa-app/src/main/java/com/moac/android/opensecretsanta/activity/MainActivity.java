package com.moac.android.opensecretsanta.activity;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import com.google.common.primitives.Longs;
import com.moac.android.inject.dagger.InjectingActivity;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.adapter.DrawerButtonItem;
import com.moac.android.opensecretsanta.adapter.DrawerListAdapter;
import com.moac.android.opensecretsanta.adapter.GroupDetailsRow;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.draw.MemberEditor;
import com.moac.android.opensecretsanta.fragment.MemberListFragment;
import com.moac.android.opensecretsanta.fragment.NotifyDialogFragment;
import com.moac.android.opensecretsanta.fragment.NotifyExecutorFragment;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.PersistableObject;
import com.moac.android.opensecretsanta.notify.NotifyAuthorization;
import com.moac.android.opensecretsanta.notify.sms.SmsPermissionsManager;
import com.moac.android.opensecretsanta.util.GroupUtils;
import com.moac.android.opensecretsanta.util.NotifyUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

// FIXME(PT) This class is too big
public class MainActivity extends InjectingActivity implements MemberListFragment.FragmentContainer, NotifyDialogFragment.FragmentContainer, MemberEditor {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String MEMBERS_LIST_FRAGMENT_TAG = "MemberListFragment";
    private static final String NOTIFY_DIALOG_FRAGMENT_TAG = "NotifyDialogFragment";
    private static final String NOTIFY_EXECUTOR_FRAGMENT_TAG = "NotifyExecutorFragment";
    private static final String SHOW_SMS_WARNING_DIALOG_SETTING_KEY = "showSmsWarningDialog";

    @Inject
    DatabaseManager mDb;

    @Inject
    SharedPreferences mSharedPreferences;

    @Inject
    SmsPermissionsManager mSmsPermissionManager;

    protected DrawerLayout mDrawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected ListView mDrawerList;
    private NotifyExecutorFragment mNotifyExecutorFragment;
    private DrawerListAdapter mDrawerListAdapter;
    private long mCurrentGroupId = Group.UNSET_ID;
    private View mDefaultSmsWarningView;

    @Override
    public void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);

        // Find or create existing worker fragment
        FragmentManager fm = getFragmentManager();

        // Find or create existing worker fragment
        mNotifyExecutorFragment = (NotifyExecutorFragment) fm.findFragmentByTag(NOTIFY_EXECUTOR_FRAGMENT_TAG);

        if (mNotifyExecutorFragment == null) {
            mNotifyExecutorFragment = NotifyExecutorFragment.create();
            fm.beginTransaction().add(mNotifyExecutorFragment, NOTIFY_EXECUTOR_FRAGMENT_TAG).commit();
        }
        initialiseUI();
    }

    private void initialiseUI() {
        setContentView(R.layout.activity_main);
        mDrawerList = (ListView) findViewById(R.id.left_drawer_list);

        // Add Groups list header - *before adapter is set*
        View headerView = getLayoutInflater().inflate(R.layout.drawer_section_header_view, mDrawerList, false);
        mDrawerList.addHeaderView(headerView);

        mDrawerListAdapter = new DrawerListAdapter(this);
        mDrawerList.setAdapter(mDrawerListAdapter);
        populateDrawerListView(mDrawerListAdapter);
        mDrawerList.setOnItemClickListener(new GroupItemClickListener());

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_menu_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open_accesshint,  /* "open drawer" description */
                R.string.drawer_close_accesshint) /* "close drawer" description */ {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(getString(R.string.app_name));
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setDisplayUseLogoEnabled(false);
        }

        //  Fetch the most recently used Group Id from preferences
        long groupId = mSharedPreferences.
                getLong(OpenSecretSantaApplication.MOST_RECENT_GROUP_KEY, PersistableObject.UNSET_ID);

        // Ensure most recent Group is valid
        if (groupId > PersistableObject.UNSET_ID) {
            int adapterPosition = getItemAdapterPosition(mDrawerListAdapter, groupId);
            if (adapterPosition >= 0) {
                // Check the valid list item
                mDrawerList.setItemChecked(toListViewPosition(mDrawerList, adapterPosition), true);
                showGroup(groupId, false);
            } else {
                Log.i(TAG, "Most recent groupId was invalid: " + groupId);
                mSharedPreferences.
                        edit().remove(OpenSecretSantaApplication.MOST_RECENT_GROUP_KEY).apply();
                // Show the drawer to allow Group creation/selection by user
                mDrawerLayout.openDrawer(mDrawerList);
            }
        } else {
            // Show the drawer to allow Group creation by user
            mDrawerLayout.openDrawer(mDrawerList);
        }

        mDefaultSmsWarningView = findViewById(R.id.view_default_sms_warning);
        Button fixItButton = (Button) mDefaultSmsWarningView.findViewById(R.id.button_fixIt);
        fixItButton.setOnClickListener(new FixDefaultSmsListener(this, mSmsPermissionManager));
    }

    private static int getItemAdapterPosition(DrawerListAdapter adapter, long groupId) {
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItemId(i) == groupId) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDefaultSmsWarningView.setVisibility(NotifyUtils.isDefaultSmsApp(this) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                Intent intent = new Intent(MainActivity.this, AllPreferencesActivity.class);
                slideInIntent(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onEditMember(long _memberId) {
        Intent intent = new Intent(MainActivity.this, EditActivity.class);
        intent.putExtra(Intents.MEMBER_ID_INTENT_EXTRA, _memberId);
        slideInIntent(intent);
    }

    @Override
    public void onRestrictMember(long _groupId, long _memberId) {
        Intent intent = new Intent(MainActivity.this, RestrictionsActivity.class);
        intent.putExtra(Intents.GROUP_ID_INTENT_EXTRA, _groupId);
        intent.putExtra(Intents.MEMBER_ID_INTENT_EXTRA, _memberId);
        slideInIntent(intent);
    }

    @Override
    public void requestNotifySelectionDraw(Group group, long[] memberIds) {
        requestNotifyDraw(group, memberIds, false);
    }

    private void requestNotifyDraw(Group group, long[] memberIds, boolean isAllMembers) {
        Log.i(TAG, "onNotifyDraw() - Requesting Notify member set size:" + memberIds.length);

        // Use the plural strings
        String notifyDialogTitle = isAllMembers ? getString(R.string.notify_dialog_title)
                : getString(R.string.notify_selection_dialog_title_unformatted, memberIds.length,
                getResources().getQuantityString(R.plurals.memberQuantity, memberIds.length));

        if (NotifyUtils.requiresSmsPermission(this, mDb, memberIds)) {
            // If using SMS - display warning about the SMS permissions
            boolean showSmsWarningDialog = mSharedPreferences.getBoolean(SHOW_SMS_WARNING_DIALOG_SETTING_KEY, true);
            if (showSmsWarningDialog) {
                showSmsWarningDialog(group, memberIds, notifyDialogTitle);
            } else {
                // Have already shown the message before, so just open the notify dialog
                openNotifyDialog(group, memberIds, notifyDialogTitle);
            }
        } else {
            // No need to show the warning dialog
            openNotifyDialog(group, memberIds, notifyDialogTitle);
        }
    }

    protected void openNotifyDialog(Group group, long[] memberIds, String title) {
        DialogFragment dialog = NotifyDialogFragment.create(group.getId(), memberIds, title);
        dialog.show(getFragmentManager(), NOTIFY_DIALOG_FRAGMENT_TAG);
    }

    @Override
    public void requestNotifyDraw(Group group) {
        Log.i(TAG, "onNotifyDraw() - Requesting Notify entire Group");
        // TODO Background
        List<Member> members = mDb.queryAllMembersForGroup(group.getId());
        List<Long> memberIds = new ArrayList<Long>(members.size());
        for (Member member : members) {
            memberIds.add(member.getId());
        }
        requestNotifyDraw(group, Longs.toArray(memberIds), true);
    }

    @Override
    public void executeNotifyDraw(NotifyAuthorization auth, final Group group, final long[] members) {
        mNotifyExecutorFragment.notifyDraw(auth, group, members);
    }

    @Override
    public void deleteGroup(long groupId) {
        mDb.delete(groupId, Group.class);
        populateDrawerListView(mDrawerListAdapter);

        // Show the first group, or create another if we have none
        long nextGroupId;
        Group group = mDb.queryForFirstGroup();
        if (group == null) {
            nextGroupId = createNewGroup();
        } else {
            nextGroupId = group.getId();
            int adapterPosition = getItemAdapterPosition(mDrawerListAdapter, nextGroupId);
            mDrawerList.setItemChecked(toListViewPosition(mDrawerList, adapterPosition), true);

        }
        showGroup(nextGroupId, false);
    }

    @Override
    public void renameGroup(long groupId, String newGroupName) {
        mDb.updateGroupName(groupId, newGroupName);
        // Refresh display
        populateDrawerListView(mDrawerListAdapter);
        showGroup(mCurrentGroupId, true);
    }

    private void populateDrawerListView(DrawerListAdapter drawerListAdapter) {

        List<DrawerListAdapter.Item> drawerListItems = new ArrayList<DrawerListAdapter.Item>();

        // Add "Add Group" button item
        Drawable addIcon = getResources().getDrawable(R.drawable.ic_action_add_group);
        drawerListItems.add(new DrawerButtonItem(addIcon, getString(R.string.add_group), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked Add Group Button");
                long id = createNewGroup();
                showGroup(id, false);
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        }));

        // Add each Group item
        List<Group> groups = mDb.queryAll(Group.class);
        Log.v(TAG, "initialiseUI() - group count: " + groups.size());
        for (Group g : groups) {
            drawerListItems.add(new GroupDetailsRow(g.getId(), g.getName(), g.getCreatedAt()));
        }
        drawerListAdapter.clear();
        drawerListAdapter.addAll(drawerListItems);
    }

    private long createNewGroup() {
        Log.i(TAG, "Creating new Group");
        // Create a new Group with incrementing name
        Group group = GroupUtils.createIncrementingGroup(mDb, getString(R.string.base_group_name));

        // Create corresponding adapter view model entry
        GroupDetailsRow item = new GroupDetailsRow(group.getId(), group.getName(), group.getCreatedAt());
        mDrawerListAdapter.add(item);

        // Select the item in the list view
        int adapterPosition = mDrawerListAdapter.getPosition(item);
        mDrawerList.setItemChecked(toListViewPosition(mDrawerList, adapterPosition), true);

        return group.getId();
    }

    /*
     *
     * The DrawerList includes the header as a position, so when
     * we request to check select a position using the indices from the
     * adapter we need to offset it by +1 to correctly translate into the
     * ListView index space.
     */
    private static int toListViewPosition(ListView list, int adapterPosition) {
        return adapterPosition + list.getHeaderViewsCount();
    }

    @Override
    public MemberEditor getMemberEditor() {
        // FIXME for now implement as this activity.
        return this;
    }

    private class GroupItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> _parent, View _view, int _position, long _id) {
            Log.d(TAG, "onItemClick() - position: " + _position + " id: " + _id);
            if (_id <= PersistableObject.UNSET_ID)
                return;

            // Highlight the selected item, update the title, and close the drawer
            showGroup(_id, false);
            mDrawerList.setItemChecked(_position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    private void slideInIntent(Intent intent) {
        // Activity options is since API 16.
        // Got this idea from Android Dev Bytes video - https://www.youtube.com/watch?v=Ho8vk61lVIU
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        } else {
            Bundle translateBundle = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
            startActivity(intent, translateBundle);
        }
    }

    private void showGroup(long groupId, boolean forceUpdate) {
        Log.i(TAG, "showGroup() - start. groupId: " + groupId);

        //  If the correct fragment already exists
        if (groupId == mCurrentGroupId && !forceUpdate) return;

        mCurrentGroupId = groupId;

        FragmentManager fragmentManager = getFragmentManager();
        MemberListFragment existing = (MemberListFragment) fragmentManager.findFragmentByTag(MEMBERS_LIST_FRAGMENT_TAG);

        // Replace existing MemberListFragment
        // Note: Can't call replace, seems to replace ALL fragments in the layout.
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (existing != null) {
            Log.i(TAG, "showGroup() - removing existing fragment");
            transaction.remove(existing);
        }

        Log.i(TAG, "showGroup() - creating new fragment");
        MemberListFragment newFragment = MemberListFragment.create(groupId);
        transaction.add(R.id.container_fragment_content, newFragment, MEMBERS_LIST_FRAGMENT_TAG)
                .commit();

        // Update preferences to save last viewed Group
        mSharedPreferences.
                edit().putLong(OpenSecretSantaApplication.MOST_RECENT_GROUP_KEY, groupId).apply();
    }

    // Opens the default SMS app warning dialog
    private void showSmsWarningDialog(final Group group, final long[] memberIds, final String notifyTitle) {
        View dialogContentView = getLayoutInflater().inflate(R.layout.layout_dialog_sms_warning, null);
        final CheckBox dontShowAgainCheckBox = (CheckBox) dialogContentView.findViewById(R.id.checkBox_dontShowAgain);

        new AlertDialog.Builder(MainActivity.this)
                .setTitle(getString(R.string.sms_permissions_warning_title))
                .setView(dialogContentView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSharedPreferences.edit().putBoolean(SHOW_SMS_WARNING_DIALOG_SETTING_KEY, !dontShowAgainCheckBox.isChecked()).apply();
                        openNotifyDialog(group, memberIds, notifyTitle);
                    }
                }).setNegativeButton(android.R.string.cancel, null).show();
    }
}