package com.moac.android.opensecretsanta.fragment;

import android.app.*;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.*;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.activity.Intents;
import com.moac.android.opensecretsanta.adapter.MemberListAdapter;
import com.moac.android.opensecretsanta.adapter.MemberRowDetails;
import com.moac.android.opensecretsanta.adapter.SuggestionsAdapter;
import com.moac.android.opensecretsanta.content.BusProvider;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.draw.*;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.PersistableObject;
import com.moac.android.opensecretsanta.notify.NotifyStatusEvent;
import com.moac.drawengine.DrawEngine;
import com.squareup.otto.Subscribe;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.concurrency.AndroidSchedulers;
import rx.concurrency.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

public class MemberListFragment extends ListFragment implements AbsListView.MultiChoiceModeListener {

    private static final String TAG = MemberListFragment.class.getSimpleName();
    private static final String DRAW_IN_PROGRESS_KEY = "drawInProgress";
    public static final String ASSIGNMENT_FRAGMENT_KEY = "AssignmentFragment";

    private enum Mode {Building, Notify}

    private DatabaseManager mDb;
    private Group mGroup;
    private MemberListAdapter mAdapter;

    private Mode mMode = Mode.Building;
    private AutoCompleteTextView mCompleteTextView;
    private Menu mMenu; // non CAB items

    private ProgressDialog mDrawProgressDialog;
    private Subscription mDrawSubscription;

    private FragmentContainer mFragmentContainer;

    /**
     * Factory method for this fragment class
     */
    public static MemberListFragment create(long _groupId) {
        Log.i(TAG, "MemberListFragment() - factory creating for groupId: " + _groupId);
        MemberListFragment fragment = new MemberListFragment();
        Bundle args = new Bundle();
        args.putLong(Intents.GROUP_ID_INTENT_EXTRA, _groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity _activity) {
        Log.i(TAG, "onAttach()");
        super.onAttach(_activity);
        try {
            mFragmentContainer = (FragmentContainer) _activity;
        } catch(ClassCastException e) {
            throw new ClassCastException(_activity.toString() + " must implement FragmentContainer");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mDb = OpenSecretSantaApplication.getInstance().getDatabase();
        long groupId = getArguments().getLong(Intents.GROUP_ID_INTENT_EXTRA);
        mGroup = mDb.queryById(groupId, Group.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_members_list, container, false);

        mCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.add_autoCompleteTextView);
        mCompleteTextView.setThreshold(1);
        mCompleteTextView.setAdapter(new SuggestionsAdapter(getActivity()));
        mCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Member selected = (Member) mCompleteTextView.getAdapter().getItem(position);
                Log.i(TAG, "OnItemClick() - name: " + selected.getName());
                addMember(selected, mGroup);
                mCompleteTextView.setText("");
                mCompleteTextView.requestFocus(); // Keep focus for more entries
            }
        });

        TextView titleText = (TextView) view.findViewById(R.id.content_title_textview);
        titleText.setText(mGroup.getName());

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);

        // Initially don't perform check selection.
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(this);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Show visual indicator of selection and trigger CAB.
                ((ListView) parent).setItemChecked(position, true);
            }
        });
    }

    @Override
    public void onResume() {
        // FIXME This whole methods need revising, there's numerous duplicate
        // calls and even duplicate notes about duplicate calls.
        super.onResume();
        Log.i(TAG, "onResume() - registering event bus");
        // TODO A lot of this is unnecessarily rebuilding things.
        BusProvider.getInstance().register(this);
        mMode = evaluateMode();
        setHeader();
        // Populate member list
        mAdapter = new MemberListAdapter(getActivity(), R.layout.member_row, buildMemberRowDetails(mGroup.getId()));
        setListAdapter(mAdapter);
        setMenuItems();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause() - deregistering event bus");
        BusProvider.getInstance().unregister(this);
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
        setMenuItems();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.cab_member_list_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle *non-contextual* action bar selection
        switch(item.getItemId()) {
            case R.id.menu_item_clear_assignments:
                confirmClearAssignments();
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
        } catch(InvalidDrawEngineException e) {
            Log.e(TAG, "Failed to load Draw Engine: " + e.getMessage());
            Toast.makeText(getActivity(), getString(R.string.draw_engine_init_error_msg), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        // Handle contextual action bar selection
        // Some actions will end the current action mode on completion, others not.
        switch(item.getItemId()) {
            case R.id.menu_item_edit:
                doEdit(getListView().getCheckedItemIds()[0]);
                mode.finish();
                return true;
            case R.id.menu_item_restrict:
                doRestrictions(getListView().getCheckedItemIds()[0]);
                mode.finish();
                return true;
            case R.id.menu_item_delete:
                doDelete(getListView().getCheckedItemIds());
                mode.finish();
                return true;
            case R.id.menu_item_notify_selection:
                doNotify(getListView().getCheckedItemIds());
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
    public void onDestroy() {
        if(mDrawSubscription != null) {
            mDrawSubscription.unsubscribe();
            mDrawSubscription = null;
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        Log.i(TAG, "onDestroyActionMode()");
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        Log.i(TAG, "onItemCheckedStateChanged()");
        int selectedCount = getListView().getCheckedItemCount();
        boolean isCheckedSelected = hasSendableItemChecked(getListView());

        mode.setTitle(selectedCount + " selected");
        mode.getMenu().findItem(R.id.menu_item_edit).setVisible(selectedCount == 1);
        mode.getMenu().findItem(R.id.menu_item_delete).setVisible(mMode == Mode.Building);
        mode.getMenu().findItem(R.id.menu_item_notify_selection).setVisible(mMode == Mode.Notify && isCheckedSelected);
        mode.getMenu().findItem(R.id.menu_item_restrict).setVisible(mMode == Mode.Building && selectedCount == 1);
        mode.getMenu().findItem(R.id.menu_item_reveal).setVisible(mMode == Mode.Notify && selectedCount == 1);
    }

    /*
     * Bus method - receives events for changes in the notified state.
     */

    @Subscribe
    public void onNotifyStatusChanged(NotifyStatusEvent event) {
        Log.i(TAG, "onNotifyStatusChanged() - got event: " + event.getAssignment());
        populateMemberList();
    }

    private void onModeChanged() {
        setMenuItems();
        setHeader();
    }

    private void setHeader() {
        switch(mMode) {
            case Building:
                mCompleteTextView.setVisibility(View.VISIBLE);
                break;
            case Notify:
                mCompleteTextView.setVisibility(View.GONE);
                mCompleteTextView.setText("");
                break;
            default:
                break;
        }
    }

    // TODO Do in background
    private void doDelete(long[] _ids) {
        for(long id : _ids) {
            mDb.delete(id, Member.class);
        }
        invalidateAssignments(mGroup);
        populateMemberList();
    }

    private void confirmClearAssignments() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.clear_assignments_dialog, null);

        builder.setTitle(R.string.clear_assignments_dialog_title);
        builder.setIcon(R.drawable.ic_menu_delete);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                invalidateAssignments(mGroup);
                populateMemberList();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        TextView dialogMsgView = (TextView) view.findViewById(R.id.clear_assignments_text);
        dialogMsgView.setText(R.string.clear_assignments_dialog_msg);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setMenuItems() {
        // Non-CAB buttons only
        if(mMenu != null) {
            mMenu.findItem(R.id.menu_item_draw).setVisible(mMode == Mode.Building);
            mMenu.findItem(R.id.menu_item_notify_group).setVisible(mMode == Mode.Notify && hasSendableItem(mAdapter));
            mMenu.findItem(R.id.menu_item_clear_assignments).setVisible(mMode == Mode.Notify);
        }
    }

    private void invalidateAssignments(Group group) {
        // FIXME This will clobber the Group's message if they have saved to DB
        // during notify.
        group.setDrawDate(Group.UNSET_DATE);
        mDb.update(group);
        mDb.deleteAllAssignmentsForGroup(group.getId());
        mMode = Mode.Building;
        onModeChanged();
    }

    private void doNotify(long[] _memberIds) {
        if(_memberIds != null) {
            requestNotifyDraw(mGroup, _memberIds);
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
                  Toast.makeText(getActivity(), getString(R.string.draw_failed_msg), Toast.LENGTH_SHORT).show();
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

    private void doEdit(long _memberId) {
        getMemberEditor().onEditMember(_memberId);
    }

    private void doRestrictions(long _memberId) {
        getMemberEditor().onRestrictMember(mGroup.getId(), _memberId);
    }

    private void doReveal(long _memberId) {
        Member giver = mDb.queryById(_memberId, Member.class);
        Log.i(TAG, "doReveal(): memberId: " + _memberId);
        Log.i(TAG, "doReveal(): giver name: " + giver.getName());

        Assignment assignment = mDb.queryAssignmentForMember(_memberId);
        long _receiverId = assignment.getReceiverMemberId();
        Member receiver = mDb.queryById(_receiverId, Member.class);
        String avatarUri = null;
        // TODO Check the validity of URIs with various values. Write Utils method.
        if(receiver.getContactId() != PersistableObject.UNSET_ID && receiver.getLookupKey() != null) {
            Uri lookupUri = ContactsContract.Contacts.getLookupUri(receiver.getContactId(), receiver.getLookupKey());
            Uri contactUri = ContactsContract.Contacts.lookupContact(getActivity().getContentResolver(), lookupUri);
            avatarUri = contactUri.toString();
        }
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = AssignmentFragment.create(giver.getName(), receiver.getName(), avatarUri);
        dialog.show(getFragmentManager(), ASSIGNMENT_FRAGMENT_KEY);

        // Set as Revealed
        assignment.setSendStatus(Assignment.Status.Revealed);
        mDb.update(assignment);

        // Reload list
        populateMemberList();
    }

    // TODO Make calls do this asynchronously
    private void populateMemberList() {
        getListView().clearChoices();
        mAdapter.clear();
        mAdapter.addAll(buildMemberRowDetails(mGroup.getId()));
        mAdapter.notifyDataSetChanged();
    }

    private List<MemberRowDetails> buildMemberRowDetails(long _groupId) {
        List<MemberRowDetails> rows = new ArrayList<MemberRowDetails>();
        List<Member> members = mDb.queryAllMembersForGroup(_groupId);
        for(Member member : members) {
            Assignment assignment = mDb.queryAssignmentForMember(member.getId());
            MemberRowDetails row = new MemberRowDetails(member, assignment);
            rows.add(row);
        }
        // TODO Sort on insertion
        Collections.sort(rows);
        return rows;
    }

    public long getGroupId() {
        return mGroup.getId();
    }

    private void addMember(Member _member, Group _group) {
        if(isNullOrEmpty(_member.getName()))
            return;

        final String msg;
        _member.setGroup(_group);

        // Test to see if we already have this member in the group.
        Member existing = mDb.queryMemberWithNameForGroup(_group.getId(), _member.getName());

        if(existing != null) {
            msg = String.format(getString(R.string.duplicate_name_msg), _member.getName());
        } else {
            long id = mDb.create(_member);
            if(id != PersistableObject.UNSET_ID) {
                msg = String.format(getString(R.string.member_add_msg), _member.getName());
                invalidateAssignments(_group);
                populateMemberList();
            } else {
                msg = String.format(getString(R.string.failed_add_member_msg), _member.getName());
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defaultName = getActivity().getString(R.string.defaultDrawEngine);
        String classname = prefs.getString("engine_preference",
          defaultName);

        Log.i(TAG, "getCurrentDrawEngine() - setting draw engine to: " + classname);

        try {
            return DrawEngineFactory.createDrawEngine(classname);
        } catch(InvalidDrawEngineException e) {
            // Error: If we weren't attempting to load the default name, then try that instead
            if(!classname.equals(defaultName)) {
                Log.w(TAG, "Failed to initialise draw engine class: " + classname);
                try {
                    // Try to set the default then.
                    DrawEngine engine = DrawEngineFactory.createDrawEngine(defaultName);
                    // Success - update preference to use the default.
                    prefs.edit().putString("engine_preference", defaultName).commit();
                    return engine;
                } catch(InvalidDrawEngineException ideexp2) {
                    Log.e(TAG, "Unable to initialise default draw engine class: " + classname, ideexp2);
                    throw ideexp2;
                }
            }
            throw e;
        }
    }

    // Gah...
    private boolean hasSendableItemChecked(ListView list) {
        SparseBooleanArray checkedPositions = list.getCheckedItemPositions();
        for(int i = 0; i < checkedPositions.size(); i++) {
            int position = checkedPositions.keyAt(i);
            boolean isChecked = checkedPositions.valueAt(i);
            if(isChecked && ((MemberRowDetails) (list.getAdapter().getItem(position))).getMember().getContactMethod().isSendable()) {
                Log.v(TAG, "hasSendableItemChecked() - position checked: " + position);
                return true;
            }
        }
        return false;
    }

    private boolean hasSendableItem(MemberListAdapter adapter) {
        for(int i = 0; i < adapter.getCount(); i++) {
            MemberRowDetails rowDetails = adapter.getItem(i);
            if(rowDetails.getMember().getContactMethod().isSendable()) {
                return true;
            }
        }
        return false;
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
        mFragmentContainer.requestNotifyDraw(group, memberIds);
    }

    public interface FragmentContainer {
        MemberEditor getMemberEditor();

        void requestNotifyDraw(Group group);

        void requestNotifyDraw(Group group, long[] memberIds);
    }
}
