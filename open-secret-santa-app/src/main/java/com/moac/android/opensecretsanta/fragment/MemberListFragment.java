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
import com.moac.android.opensecretsanta.activity.DrawManager;
import com.moac.android.opensecretsanta.adapter.MemberListAdapter;
import com.moac.android.opensecretsanta.adapter.MemberRowDetails;
import com.moac.android.opensecretsanta.adapter.SuggestionsAdapter;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.PersistableObject;

import java.util.ArrayList;
import java.util.List;

public class MemberListFragment extends ListFragment implements AbsListView.MultiChoiceModeListener {

    private static final String TAG = MemberListFragment.class.getSimpleName();

    private enum Mode {
        Building, Notify
    }

    private Group mGroup;
    private DatabaseManager mDb;
    private DrawManager mDrawManager;
    private AutoCompleteTextView mCompleteTextView;

    private Mode mMode = Mode.Building;

    /**
     * Factory method for this fragment class
     */
    public static MemberListFragment create(long _groupId) {
        Log.i(TAG, "MemberListFragment() - factory creating for id: " + _groupId);
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
            mDrawManager = (DrawManager) _activity;
        } catch(ClassCastException e) {
            throw new ClassCastException(_activity.toString() + " must implement DrawManager");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mDb = OpenSecretSantaApplication.getDatabase();
        long groupId = getArguments().getLong(Intents.GROUP_ID_INTENT_EXTRA);
        mGroup = mDb.queryById(groupId, Group.class);
        mMode = evaluateMode();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        setRetainInstance(true);
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
            }
        });
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // Allow selection mode
                getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

                // Visual indicator of selection and launch CAB.
                ((ListView) parent).setItemChecked(position, true);
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        Log.i(TAG, "onStart() - loading members for groupId: " + mGroup.getId());
        super.onStart();
        loadMembers(mGroup.getId());
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView()");
        // TODO Is this necessary now we are using the CHOICE_MODE_MULTIPLE_MODAL trigger?
        // Force the ending of the CAB
        getListView().clearChoices();
        getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
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
        switch (item.getItemId()) {
            case R.id.menu_draw:
                doDraw(mGroup);
                return true;
            case R.id.menu_notify:
                doNotifyAll(mGroup);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void doNotifyAll(Group group) {
        // TODO Implement
        //mDrawManager.onNotifyDraw();
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        // Handle contextual action bar selection
        switch(item.getItemId()) {
            case R.id.menu_edit:
                mode.finish();
                return true;
            case R.id.menu_restrictions:
                doRestrictions(mGroup.getId(), getListView().getCheckedItemIds()[0]);
                mode.finish();
                return true;
            case R.id.menu_delete:
                doDelete(getListView().getCheckedItemIds());
                mode.finish();
                loadMembers(mGroup.getId());
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
    }

    // TODO Do in background & add confirm dialog
    private void doDelete(long[] _ids) {
        for(long id : _ids) {
            mDb.delete(id, Member.class);
        }
        invalidateAssignments(mGroup.getId());
        Toast.makeText(getActivity(), _ids.length + " deleted", Toast.LENGTH_SHORT).show();
    }

    private void invalidateAssignments(long _groupId) {
        mDb.deleteAllAssignmentsForGroup(_groupId);
        mMode = Mode.Building;
    }

    private void doNotify(long[] _memberIds) {
        if(_memberIds != null)
            mDrawManager.onNotifyDraw(_memberIds);
    }

    private void doDraw(Group _group) {
        mDrawManager.onRequestDraw(_group);
    }

    private void doRestrictions(long _groupId, long _memberId) {
        mDrawManager.onRestrictMember(_groupId, _memberId);
    }

    private void doReveal(long _memberId) {
        // TODO Mark as seen.
        Member giver = mDb.queryById(_memberId, Member.class);
        Log.i(TAG, "doReveal(): memberId: " + _memberId);
        Log.i(TAG, "doReveal(): giver name: " + giver.getName());

        Assignment assignment = mDb.queryAssignmentForMember(_memberId);
        long _receiverId = assignment.getReceiverMemberId();
        Member receiver = mDb.queryById(_receiverId, Member.class);
        Uri contactUri = null;
        // TODO Check the validity of URIs with various values. Write Utils method.
        if(receiver.getContactId() != PersistableObject.UNSET_ID && receiver.getLookupKey() != null) {
            Uri lookupUri = ContactsContract.Contacts.getLookupUri(receiver.getContactId(), receiver.getLookupKey());
            contactUri = ContactsContract.Contacts.lookupContact(getActivity().getContentResolver(), lookupUri);
        }
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new AssignmentFragment(giver.getName(), receiver.getName(), contactUri);
        dialog.show(getFragmentManager(), "AssignmentFragment");

        // Set as Revealed
        assignment.setSendStatus(Assignment.Status.Revealed);
        mDb.update(assignment);
        loadMembers(mGroup.getId());
    }

    // TODO Make calls do this asynchronously
    private void loadMembers(long _groupId) {
        List<MemberRowDetails> rows = buildMemberRowDetails(_groupId);
        setListAdapter(new MemberListAdapter(getActivity(), R.layout.member_row, rows));
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

        // Test to see if we already have this member in the gorup.
        Member existing = mDb.queryMemberWithNameForGroup(_group.getId(), _member.getName());

        if(existing != null) {
            msg = String.format(getString(R.string.duplicate_name_msg), _member.getName());
        } else {
            long id = mDb.create(_member);
            if(id != PersistableObject.UNSET_ID) {
                msg = String.format(getString(R.string.member_add_msg), _member.getName());
                invalidateAssignments(_group.getId());
                loadMembers(_group.getId());
            } else {
               msg = String.format(getString(R.string.failed_add_member_msg), _member.getName());
            }
        }
        Toast toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void onAssignmentsAvailable() {
        // Move to Notify mode
        mMode = Mode.Notify;
        loadMembers(mGroup.getId());
        // TODO Show Notify button
    }

    public void onAssignmentsCleared() {
        // Revert to building mode
        mMode = Mode.Building;
        loadMembers(mGroup.getId());
        // TODO Hide Notify button
    }

    private Mode evaluateMode() {
       return mDb.queryHasAssignmentsForGroup(mGroup.getId()) ? Mode.Notify : Mode.Building;
    }
}
