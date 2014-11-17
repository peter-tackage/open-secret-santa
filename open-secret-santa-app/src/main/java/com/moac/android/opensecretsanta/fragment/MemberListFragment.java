package com.moac.android.opensecretsanta.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.moac.android.inject.dagger.InjectingListFragment;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.activity.Intents;
import com.moac.android.opensecretsanta.adapter.MemberListAdapter;
import com.moac.android.opensecretsanta.adapter.MemberRowDetails;
import com.moac.android.opensecretsanta.adapter.SuggestionsAdapter;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.draw.DefaultDrawExecutor;
import com.moac.android.opensecretsanta.draw.DrawEngineFactory;
import com.moac.android.opensecretsanta.draw.DrawExecutor;
import com.moac.android.opensecretsanta.draw.DrawResultEvent;
import com.moac.android.opensecretsanta.draw.InvalidDrawEngineException;
import com.moac.android.opensecretsanta.draw.MemberEditor;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.PersistableObject;
import com.moac.android.opensecretsanta.notify.NotifyStatusEvent;
import com.moac.android.opensecretsanta.util.validator.GroupNameValidator;
import com.moac.drawengine.DrawEngine;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.concurrency.AndroidSchedulers;
import rx.concurrency.Schedulers;

import static com.moac.android.opensecretsanta.util.Utils.safeUnsubscribe;

// TODO(PT) This class is too big
public class MemberListFragment extends InjectingListFragment {

    private static final String TAG = MemberListFragment.class.getSimpleName();
    private static final String DRAW_IN_PROGRESS_KEY = "drawInProgress";
    public static final String ASSIGNMENT_FRAGMENT_KEY = "AssignmentFragment";

    private ActionMode mActionMode;

    private enum Mode {Building, Notify}

    @Inject
    DatabaseManager mDb;

    @Inject
    SharedPreferences mSharedPreferences;

    @Inject
    Bus mBus;

    // FIXME Perhaps hold this in a separate model with listeners/observers
    private Mode mMode = Mode.Building;
    private Group mGroup;

    private MemberListAdapter mAdapter;
    private AutoCompleteTextView mAutoCompleteTextView;
    private Menu mMenu; // non CAB items

    private ProgressDialog mDrawProgressDialog;
    private Subscription mDrawSubscription;

    private FragmentContainer mFragmentContainer;

    public static MemberListFragment create(long _groupId) {
        Log.i(TAG, "MemberListFragment() - factory creating for groupId: " + _groupId);
        MemberListFragment fragment = new MemberListFragment();
        Bundle args = new Bundle();
        args.putLong(Intents.GROUP_ID_INTENT_EXTRA, _groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        Log.i(TAG, "onAttach()");
        super.onAttach(activity);
        try {
            mFragmentContainer = (FragmentContainer) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement FragmentContainer");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        long groupId = getArguments().getLong(Intents.GROUP_ID_INTENT_EXTRA);
        mGroup = mDb.queryById(groupId, Group.class);

        // Initialise content dependent on injection
        TextView titleText = (TextView) getView().findViewById(R.id.textView_groupName);
        titleText.setText(mGroup.getName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_members_list, container, false);
        bindViews(view);
        return view;
    }

    private void bindViews(View view) {
        mAutoCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView_addMember);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
        configureViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume() - registering event bus");
        mBus.register(this);
        populateUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause() - deregistering event bus");
        mBus.unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(DRAW_IN_PROGRESS_KEY, mDrawSubscription != null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(TAG, "onCreateOptionsMenu()");
        inflater.inflate(R.menu.action_bar_menu, menu);
        inflater.inflate(R.menu.dropdown_menu, menu);
        mMenu = menu;
        setMenuItemsForMode(mMode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle *non-contextual* action bar selection
        switch (item.getItemId()) {
            case R.id.menu_item_clear_assignments:
                confirmClearAssignments();
                return true;
            case R.id.menu_item_delete_group:
                confirmDeleteGroup();
                return true;
            case R.id.menu_item_rename_group:
                openRenameGroupDialog();
                return true;
            case R.id.menu_item_draw:
                attemptDraw();
                return true;
            case R.id.menu_item_notify_group:
                doNotifyAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void attemptDraw() {
        try {
            mDrawSubscription = doDraw();
        } catch (InvalidDrawEngineException e) {
            Log.e(TAG, "Failed to load Draw Engine: " + e.getMessage());
            Toast.makeText(getActivity(), getString(R.string.draw_engine_init_error_msg), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        if (mActionMode != null) mActionMode.finish();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        safeUnsubscribe(mDrawSubscription);
        setListAdapter(null);
        super.onDestroy();
    }

    /*
     * Bus method - receives events for changes in the notified state.
     */

    @Subscribe
    public void onNotifyStatusChanged(NotifyStatusEvent event) {
        Log.i(TAG, "onNotifyStatusChanged() - got event: " + event.getAssignment());
        populateMemberList();
    }

    private void configureViews() {
        mAutoCompleteTextView.setThreshold(1);
        mAutoCompleteTextView.setAdapter(new SuggestionsAdapter(getActivity()));
        mAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Member member = (Member) mAutoCompleteTextView.getAdapter().getItem(position);
                Log.i(TAG, "OnItemClick() - name: " + member.getName());
                addMember(member, mGroup);
                mAutoCompleteTextView.setText("");
                mAutoCompleteTextView.requestFocus(); // Keep focus for more entries
            }
        });

        // Configure Adapter
        mAdapter = new MemberListAdapter(getActivity(), R.layout.list_item_member);
        setListAdapter(mAdapter);

        // Initially don't perform check selection.
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.cab_member_list_menu, menu);
                mActionMode = mode;
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Handle contextual action bar selection
                // Some actions will end the current action mode on completion, others not.
                switch (item.getItemId()) {
                    case R.id.menu_item_edit:
                        doEdit(getListView().getCheckedItemIds()[0]);
                        mode.finish();
                        return true;
                    case R.id.menu_item_restrict:
                        doRestrictions(getListView().getCheckedItemIds()[0]);
                        mode.finish();
                        return true;
                    case R.id.menu_item_delete:
                        confirmDeleteMembers(getListView().getCheckedItemIds());
                        mode.finish();
                        return true;
                    case R.id.menu_item_notify_selection:
                        doNotify(getListView().getCheckedItemPositions());
                        mode.finish();
                        return true;
                    case R.id.menu_item_reveal:
                        doReveal(getListView().getCheckedItemIds()[0]);
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                Log.i(TAG, "onDestroyActionMode()");
                mode.getMenu().clear();
                mActionMode = null;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                Log.i(TAG, "onItemCheckedStateChanged()");
                int checkedItemCount = getListView().getCheckedItemCount();
                boolean isSingleItemChecked = checkedItemCount == 1;
                boolean hasSendableItemChecked = hasSendableItemChecked(getListView());
                mode.setTitle(String.format(getString(R.string.list_selection_count_title_unformatted), checkedItemCount));
                mode.getMenu().findItem(R.id.menu_item_edit).setVisible(isSingleItemChecked);
                mode.getMenu().findItem(R.id.menu_item_delete).setVisible(mMode == Mode.Building);
                mode.getMenu().findItem(R.id.menu_item_notify_selection).setVisible(mMode == Mode.Notify && hasSendableItemChecked);
                mode.getMenu().findItem(R.id.menu_item_restrict).setVisible(mMode == Mode.Building && isSingleItemChecked);
                mode.getMenu().findItem(R.id.menu_item_reveal).setVisible(mMode == Mode.Notify && isSingleItemChecked);
            }
        });
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Show visual indicator of selection and trigger CAB.
                Log.i(TAG, "onItemClick() - position: " + position);
                ((ListView) parent).setItemChecked(position, true);
            }
        });
    }

    private void confirmDeleteMembers(final long[] memberIds) {
        String memberQuantity = getResources().getQuantityString(R.plurals.memberQuantity, memberIds.length);
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.delete_selection_dialog_title_unformatted, memberIds.length,
                        memberQuantity))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doDelete(memberIds);
                    }
                })
                .setMessage(getString(R.string.delete_members_confirm_msg_unformatted, memberQuantity))
                .create()
                .show();
    }

    private void populateUI() {
        mMode = evaluateMode();
        setHeader(); // depends on mode
        // Populate member list
        populateMemberList();
        setMenuItemsForMode(mMode); // depends on adapter contents
    }

    private void onModeChanged() {
        setMenuItemsForMode(mMode);
        setHeader();
    }

    private void setHeader() {
        switch (mMode) {
            case Building:
                mAutoCompleteTextView.setVisibility(View.VISIBLE);
                break;
            case Notify:
                mAutoCompleteTextView.setVisibility(View.GONE);
                mAutoCompleteTextView.setText("");
                break;
            default:
                break;
        }
    }

    // TODO Do in background
    private void doDelete(long[] ids) {
        if (ids == null || ids.length == 0)
            return;

        for (long id : ids) {
            mDb.delete(id, Member.class);
        }
        // FIXME This is seems to be required to delete the last one.
        getListView().clearChoices();
        invalidateAssignments(mGroup);
        populateMemberList();
    }

    private void confirmClearAssignments() {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_clear_assignments, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.clear_assignments_dialog_title)
                .setIcon(R.drawable.ic_action_delete)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        invalidateAssignments(mGroup);
                        populateMemberList();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        Dialog dialog = builder.create();
        dialog.show();
    }

    private void confirmDeleteGroup() {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_delete_group, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.delete_group_dialog_title)
                .setIcon(R.drawable.ic_action_delete)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mFragmentContainer.deleteGroup(mGroup.getId());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        Dialog dialog = builder.create();
        dialog.show();
    }

    private void openRenameGroupDialog() {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_rename_group, null);
        final EditText groupNameEditText = (EditText) view.findViewById(R.id.editText_groupName);
        groupNameEditText.setText(mGroup.getName());
        groupNameEditText.setSelection(groupNameEditText.getText().length());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.rename_group_dialog_title)
                .setIcon(R.drawable.ic_action_edit)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Handled in custom listener defined after show()
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Override the dialog positive button behaviour to allow for custom validation
        Button okButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear error
                groupNameEditText.setError(null);

                String newGroupName = groupNameEditText.getText().toString().trim();

                // Hasn't change, just dismiss - allow case change for the same group
                if (newGroupName.equals(mGroup.getName())) {
                    dialog.dismiss();
                    return;
                }

                // Validate new Group name
                GroupNameValidator validator = new GroupNameValidator(mDb, mGroup.getId(), newGroupName);
                if (!validator.isValid()) {
                    groupNameEditText.setError(validator.getMsg());
                } else {
                    mFragmentContainer.renameGroup(mGroup.getId(), newGroupName);
                    dialog.dismiss();
                }
            }
        });

    }

    private void setMenuItemsForMode(Mode mode) {
        // Set visibility of Non-CAB buttons only
        if (mMenu != null) {
            mMenu.findItem(R.id.menu_item_draw).setVisible(mode == Mode.Building);
            mMenu.findItem(R.id.menu_item_notify_group).setVisible(mode == Mode.Notify && hasSendableItem(mAdapter));
            mMenu.findItem(R.id.menu_item_clear_assignments).setVisible(mode == Mode.Notify);
        }
    }

    private void invalidateAssignments(Group group) {
        // FIXME This will clobber the Group's message if they have saved to DB during notify.
        group.setDrawDate(Group.UNSET_DATE);
        mDb.update(group);
        mDb.deleteAllAssignmentsForGroup(group.getId());
        mMode = Mode.Building;
        onModeChanged();
    }

    private void doNotify(SparseBooleanArray checkedMemberPositions) {
        if (checkedMemberPositions == null || checkedMemberPositions.size() == 0) return;

        long[] sendableMemberIds = checkedMemberPositionToSendableIds(checkedMemberPositions);
        if (sendableMemberIds.length != 0) {
            // Remove non-sendable members
            requestNotifyDraw(mGroup, sendableMemberIds);
        }
    }

    private void doNotifyAll() {
        requestNotifyDraw(mGroup);
    }

    private Subscription doDraw() throws InvalidDrawEngineException {
        Log.v(TAG, "doDraw() - start");
        // Find the current DrawEngine to use
        DrawEngine engine = getCurrentDrawEngine();

        // Show the progress dialog
        showDrawProgressDialog();

        DrawExecutor drawExecutor = new DefaultDrawExecutor(mDb);
        Observable<DrawResultEvent> ob = drawExecutor.requestDraw(engine, mGroup);
        return ob.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).
                subscribe(new Observer<DrawResultEvent>() {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "onCompleted");
                        mDrawSubscription = null;
                        mDrawProgressDialog.dismiss();
                        mDrawProgressDialog = null;
                        Log.i(TAG, "onDrawResult() - got event");
                        mMode = evaluateMode();
                        onModeChanged();
                        populateMemberList();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "onError");
                        mDrawProgressDialog.dismiss();
                        Toast.makeText(getActivity(), getString(R.string.draw_failed_msg), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(DrawResultEvent args) {
                        Log.i(TAG, "onNext");
                        Toast.makeText(getActivity(), getString(R.string.draw_success_msg), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDrawProgressDialog() {
        Log.v(TAG, "showDrawProgressDialog() - start");
        mDrawProgressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.draw_in_progress_msg), true);
    }

    private void doEdit(long memberId) {
        getMemberEditor().onEditMember(memberId);
    }

    private void doRestrictions(long memberId) {
        getMemberEditor().onRestrictMember(mGroup.getId(), memberId);
    }

    private void doReveal(long memberId) {
        Member giver = mDb.queryById(memberId, Member.class);
        Log.i(TAG, "doReveal(): memberId: " + memberId);
        Log.i(TAG, "doReveal(): giver name: " + giver.getName());

        Assignment assignment = mDb.queryAssignmentForMember(memberId);
        long _receiverId = assignment.getReceiverMemberId();
        Member receiver = mDb.queryById(_receiverId, Member.class);
        String avatarUri = null;
        // TODO Check the validity of URIs with various values. Write Utils method.
        if (receiver.getContactId() != PersistableObject.UNSET_ID && receiver.getLookupKey() != null) {
            Uri lookupUri = ContactsContract.Contacts.getLookupUri(receiver.getContactId(), receiver.getLookupKey());
            Uri contactUri = ContactsContract.Contacts.lookupContact(getActivity().getContentResolver(), lookupUri);
            avatarUri = contactUri.toString();
        }
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = RevealDialogFragment.create(giver.getName(), receiver.getName(), avatarUri);
        dialog.show(getFragmentManager(), ASSIGNMENT_FRAGMENT_KEY);

        // Set as Revealed
        assignment.setSendStatus(Assignment.Status.Revealed);
        mDb.update(assignment);

        // Reload list
        populateMemberList();
    }

    // TODO Make calls do this asynchronously
    private void populateMemberList() {
        // getListView().clearChoices();
        mAdapter.setNotifyOnChange(false);
        mAdapter.clear();
        mAdapter.addAll(buildMemberRowDetails(mGroup.getId()));
        mAdapter.notifyDataSetChanged();
    }

    private List<MemberRowDetails> buildMemberRowDetails(long groupId) {
        List<MemberRowDetails> rows = new ArrayList<>();
        List<Member> members = mDb.queryAllMembersForGroup(groupId);
        for (Member member : members) {
            Assignment assignment = mDb.queryAssignmentForMember(member.getId());
            MemberRowDetails row = new MemberRowDetails(member, assignment);
            rows.add(row);
        }
        // TODO Sort on insertion
        Collections.sort(rows);
        return rows;
    }

    private void addMember(Member member, Group group) {
        if (TextUtils.isEmpty(member.getName())) return;

        final String msg;
        member.setGroup(group);

        // Test to see if we already have this member in the group.
        Member existing = mDb.queryMemberWithNameForGroup(group.getId(), member.getName());
        if (existing != null) {
            msg = String.format(getString(R.string.duplicate_name_msg_unformatted), member.getName());
        } else {
            long id = mDb.create(member);
            if (id != PersistableObject.UNSET_ID) {
                msg = String.format(getString(R.string.member_add_msg_unformatted), member.getName());
                invalidateAssignments(group);
                populateMemberList();
            } else {
                msg = String.format(getString(R.string.failed_add_member_msg_unformatted), member.getName());
            }
        }
        Toast toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private Mode evaluateMode() {
        return mDb.queryHasAssignmentsForGroup(mGroup.getId()) ? Mode.Notify : Mode.Building;
    }

    // Returns an instance of the currently preferred DrawEngine
    private DrawEngine getCurrentDrawEngine() throws InvalidDrawEngineException {

        String defaultName = getActivity().getString(R.string.defaultDrawEngine);
        String classname = mSharedPreferences.getString("engine_preference",
                defaultName);

        Log.i(TAG, "getCurrentDrawEngine() - setting draw engine to: " + classname);

        try {
            return DrawEngineFactory.createDrawEngine(classname);
        } catch (InvalidDrawEngineException e) {
            // Error: If we weren't attempting to load the default name, then try that instead
            if (!classname.equals(defaultName)) {
                Log.w(TAG, "Failed to initialise draw engine class: " + classname);
                try {
                    // Try to set the default then.
                    DrawEngine engine = DrawEngineFactory.createDrawEngine(defaultName);
                    // Success - update preference to use the default.
                    mSharedPreferences.edit().putString("engine_preference", defaultName).apply();
                    return engine;
                } catch (InvalidDrawEngineException ideexp2) {
                    Log.e(TAG, "Unable to initialise default draw engine class: " + classname, ideexp2);
                    throw ideexp2;
                }
            }
            throw e;
        }
    }

    // Gah...
    private static boolean hasSendableItemChecked(ListView list) {
        SparseBooleanArray checkedPositions = list.getCheckedItemPositions();
        for (int i = 0; i < checkedPositions.size(); i++) {
            int position = checkedPositions.keyAt(i);
            boolean isChecked = checkedPositions.valueAt(i);
            if (isChecked && ((MemberRowDetails) (list.getAdapter().getItem(position))).getMember().getContactMethod().isSendable()) {
                Log.v(TAG, "hasSendableItemChecked() - position checked: " + position);
                return true;
            }
        }
        return false;
    }

    private static boolean hasSendableItem(MemberListAdapter adapter) {
        for (int i = 0; i < adapter.getCount(); i++) {
            MemberRowDetails rowDetails = adapter.getItem(i);
            if (rowDetails.getMember().getContactMethod().isSendable()) {
                return true;
            }
        }
        return false;
    }

    // FIXME This is terrible code
    private long[] checkedMemberPositionToSendableIds(SparseBooleanArray checkedMemberPositions) {
        // Find all the checked member ids that are sendable
        ArrayList<Long> sendableMemberIdsList = new ArrayList<>();
        for (int i = 0; i < checkedMemberPositions.size(); i++) {
            int key = checkedMemberPositions.keyAt(i);
            if (checkedMemberPositions.get(key)) {
                MemberRowDetails checkedMemberDetails = mAdapter.getItem(key);
                if (checkedMemberDetails.getMember().getContactMethod().isSendable()) {
                    sendableMemberIdsList.add(checkedMemberDetails.getMember().getId());
                }
            }
        }

        // Convert that list of Longs to long
        long[] sendableMemberIds = new long[sendableMemberIdsList.size()];
        for (int i = 0; i < sendableMemberIds.length; i++) {
            sendableMemberIds[i] = sendableMemberIdsList.get(i);
        }
        return sendableMemberIds;
    }

    /*
     * Fragment Container methods
     */

    MemberEditor getMemberEditor() {
        return mFragmentContainer.getMemberEditor();
    }

    void requestNotifyDraw(Group group) {
        mFragmentContainer.requestNotifyDraw(group);
    }

    void requestNotifyDraw(Group group, long[] memberIds) {
        mFragmentContainer.requestNotifySelectionDraw(group, memberIds);
    }

    public interface FragmentContainer {
        MemberEditor getMemberEditor();

        void requestNotifyDraw(Group group);

        void requestNotifySelectionDraw(Group group, long[] memberIds);

        void deleteGroup(long groupId);

        void renameGroup(long groupId, String newGroupName);
    }
}
