package com.moac.android.opensecretsanta.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
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
import com.moac.android.opensecretsanta.draw.AssignmentsEvent;
import com.moac.android.opensecretsanta.draw.DrawExecutor;
import com.moac.android.opensecretsanta.draw.DrawResultEvent;
import com.moac.android.opensecretsanta.draw.MemberEditor;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.PersistableObject;
import com.moac.android.opensecretsanta.notify.NotifyStatusEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class MemberListFragment extends ListFragment implements AbsListView.MultiChoiceModeListener {

    private static final String TAG = MemberListFragment.class.getSimpleName();

    private List<MemberRowDetails> mRows = new ArrayList<MemberRowDetails>();
    private MemberListAdapter mAdapter;

    private enum Mode {
        Building, Notify
    }

    private Group mGroup;
    private DatabaseManager mDb;
    private FragmentContainer mFragmentContainer;
    private AutoCompleteTextView mCompleteTextView;

    private Mode mMode = Mode.Building;

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
        View view = inflater.inflate(R.layout.members_list_fragment, container, false);

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
        getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
        getListView().setMultiChoiceModeListener(this);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Remove this for now.
                // TODO View or edit?
            }
        });
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Trigger CAB
                getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

                // Visual indicator of selection and trigger CAB.
                ((ListView) parent).setItemChecked(position, true);
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        mMode = evaluateMode();
        // Populate member list
        mRows = buildMemberRowDetails(mGroup.getId());
        mAdapter = new MemberListAdapter(getActivity(), R.layout.member_row, mRows);
        setListAdapter(mAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.draw_menu, menu);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.edit_member_menu, menu);
        inflater.inflate(R.menu.notify_member_menu, menu);
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
            case R.id.menu_draw:
                doDraw();
                return true;
            case R.id.menu_notify:
                doNotifyAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        // Handle contextual action bar selection
        // Some actions will end the current action mode on completion, others not.
        switch(item.getItemId()) {
            case R.id.menu_edit:
                doEdit(getListView().getCheckedItemIds()[0]);
                mode.finish();
                return true;
            case R.id.menu_restrictions:
                doRestrictions(getListView().getCheckedItemIds()[0]);
                mode.finish();
                return true;
            case R.id.menu_delete:
                doDelete(getListView().getCheckedItemIds());
                mode.finish();
                return true;
            case R.id.menu_notify_selection:
                doNotify(getListView().getCheckedItemIds());
                mode.finish();
                return true;
            case R.id.menu_reveal:
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
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        Log.i(TAG, "onItemCheckedStateChanged()");
        int selectedCount = getListView().getCheckedItemCount();
        mode.setTitle(selectedCount + " selected");
        mode.getMenu().setGroupVisible(R.id.menu_group_single_selection, (selectedCount == 1));
        mode.getMenu().setGroupVisible(R.id.menu_group_notify, mMode == Mode.Notify);
        mode.getMenu().setGroupVisible(R.id.menu_group_notify_single_selection, mMode == Mode.Notify && (selectedCount == 1));
        MenuItem restrictionsMenu = mode.getMenu().findItem(R.id.menu_restrictions);
        restrictionsMenu.setVisible(getListAdapter().getCount() > 1);
    }

    /*
     * Bus methods
     */

    @Subscribe
    public void onAssignmentChanged(AssignmentsEvent event) {
        Log.i(TAG, "onAssignmentChanged() - got event");
        mMode = evaluateMode();
        populateMemberList();
    }

    @Subscribe
    public void onDrawResult(DrawResultEvent event) {
        Log.i(TAG, "onDrawResult() - got event");
        mMode = evaluateMode();
        populateMemberList();
        if(event.isSuccess()) {
            Toast.makeText(getActivity(), "Draw Success!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Draw Failed :(", Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void onNotifyStatusChanged(NotifyStatusEvent event) {
        Log.i(TAG, "onNotifyStatusChanged() - got event: " + event.getAssignment());
        populateMemberList();
    }

    // TODO Do in background & add confirm dialog
    private void doDelete(long[] _ids) {
        for(long id : _ids) {
            mDb.delete(id, Member.class);
        }
        invalidateAssignments(mGroup);
        populateMemberList();
    }

    private void invalidateAssignments(Group group) {
        group.setDrawDate(Group.UNSET_DATE);
        mDb.update(group);
        mDb.deleteAllAssignmentsForGroup(group.getId());
        mMode = Mode.Building;
    }

    private void doNotify(long[] _memberIds) {
        if(_memberIds != null) {
            getMemberEditor().onNotifyDraw(mGroup, _memberIds);
        }
    }

    private void doNotifyAll() {
        getMemberEditor().onNotifyDraw(mGroup);
    }

    private void doDraw() {
        getDrawExecutor().requestDraw(mGroup);
    }

    private void doEdit(long _memberId) {
        getMemberEditor().onEditMember(mGroup.getId(), _memberId);
    }

    private void doRestrictions(long _memberId) {
        getMemberEditor().onRestrictMember(mGroup.getId(), _memberId);
    }

    private void doReveal(long _memberId) {
        // TODO Mark as seen.
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
        dialog.show(getFragmentManager(), "AssignmentFragment");

        // Set as Revealed
        assignment.setSendStatus(Assignment.Status.Revealed);
        mDb.update(assignment);

        // Reload list
        populateMemberList();
    }

    // TODO Make calls do this asynchronously
    private void populateMemberList() {
        mRows.clear();
        mRows.addAll(buildMemberRowDetails(mGroup.getId()));
        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private List<MemberRowDetails> buildMemberRowDetails(long _groupId) {
        List<MemberRowDetails> rows = new ArrayList<MemberRowDetails>();
        List<Member> members = mDb.queryAllMembersForGroup(_groupId);
        for(Member member : members) {
            Assignment assignment = mDb.queryAssignmentForMember(member.getId());
            MemberRowDetails row = new MemberRowDetails(member, assignment);
            rows.add(row);
        }
        return rows;
    }

    public long getGroupId() {
        return mGroup.getId();
    }

    private void addMember(Member _member, Group _group) {
        final String msg;
        _member.setGroup(_group);

        // Test to see if we already have this member in the group.
        // TODO This should test for equality in a case insensitive way
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

    /*
     * Fragment Container methods
     */

    public DrawExecutor getDrawExecutor() {
        return mFragmentContainer.getDrawExecutor();
    }

    public MemberEditor getMemberEditor() {
        return mFragmentContainer.getMemberEditor();
    }

    public interface FragmentContainer {
        DrawExecutor getDrawExecutor();
        MemberEditor getMemberEditor();
    }
}
