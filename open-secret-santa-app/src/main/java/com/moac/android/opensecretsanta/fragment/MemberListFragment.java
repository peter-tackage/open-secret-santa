package com.moac.android.opensecretsanta.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.activity.Intents;
import com.moac.android.opensecretsanta.activity.OnEditMemberListener;
import com.moac.android.opensecretsanta.adapter.MemberListAdapter;
import com.moac.android.opensecretsanta.adapter.SuggestionsAdapter;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.PersistableObject;

import java.util.List;

public class MemberListFragment extends ListFragment implements AbsListView.MultiChoiceModeListener {

    private static final String TAG = MemberListFragment.class.getSimpleName();

    private Group mGroup;
    private DatabaseManager mDb;
    private OnEditMemberListener mEditMemberListener;
    private AutoCompleteTextView mCompleteTextView;

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
        super.onAttach(_activity);
        try {
            mEditMemberListener = (OnEditMemberListener) _activity;
        } catch(ClassCastException e) {
            throw new ClassCastException(_activity.toString() + " must implement OnEditMemberListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDb = OpenSecretSantaApplication.getDatabase();
        long groupId = getArguments().getLong(Intents.GROUP_ID_INTENT_EXTRA);
        mGroup = mDb.queryById(groupId, Group.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
                addMember(selected);
                mCompleteTextView.setText("");
                mCompleteTextView.requestFocus(); // Keep focus for more entries
                Toast.makeText(getActivity(), selected.getName() + " added", Toast.LENGTH_SHORT).show();
            }
        });

        TextView titleText = (TextView) view.findViewById(R.id.content_title_textview);
        titleText.setText(mGroup.getName());
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
        super.onStart();
        Log.i(TAG, "onActivityCreated() - loading members for groupId: " + mGroup.getId());
        loadMembers();
    }

    @Override
    public void onDestroyView() {
        // Force the ending of the CAB
        getListView().clearChoices();
        getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
        super.onDestroyView();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.edit_member_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_edit:
                mode.finish();
                return true;
            case R.id.menu_restrictions:
                doRestrictions(getListView().getCheckedItemIds()[0]);
                mode.finish();
                return true;
            case R.id.menu_delete:
                doDelete(getListView().getCheckedItemIds());
                mode.finish();
                loadMembers();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {}

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        int selectedCount = getListView().getCheckedItemCount();
        mode.setTitle(selectedCount + " selected");
        mode.getMenu().setGroupVisible(R.id.menu_single_selection_group, (selectedCount == 1));
    }

    // TODO Do in background & add confirm dialog
    private void doDelete(long[] _ids) {
        for(int i = 0; i < _ids.length; i++) {
            long memberId = _ids[i];
            mDb.delete(memberId, Member.class);
        }
        Toast.makeText(getActivity(), _ids.length + " deleted", Toast.LENGTH_SHORT).show();
    }

    private void doRestrictions(long _id) {
        mEditMemberListener.onRestrictMember(mGroup.getId(), _id);
    }

    // TODO Make this load asynchronously and somewhere else
    private void loadMembers() {
        List<Member> members = mDb.queryAllMembersForGroup(mGroup.getId());
        Log.i(TAG, "onActivityCreated() - retrieved members count: " + members.size());
        setListAdapter(new MemberListAdapter(getActivity(), R.layout.member_row, members));
    }

    public long getGroupId() {
        return mGroup.getId();
    }

    private void addMember(Member _member) {
        _member.setGroup(mGroup);
        long id = mDb.create(_member);
        if(id != PersistableObject.UNSET_ID) {
            loadMembers();
        }
    }
}
