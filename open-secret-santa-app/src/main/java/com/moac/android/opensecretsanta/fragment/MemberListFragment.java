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
import com.moac.android.opensecretsanta.activity.DrawManager;
import com.moac.android.opensecretsanta.adapter.MemberListAdapter;
import com.moac.android.opensecretsanta.adapter.SuggestionsAdapter;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.DrawResult;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.PersistableObject;

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
                Toast.makeText(getActivity(), selected.getName() + " added", Toast.LENGTH_SHORT).show();
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
                doNotify(mGroup);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            case R.id.menu_notify:
                mode.finish();
            case R.id.menu_reveal:
                mode.finish();
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
        mode.getMenu().setGroupVisible(R.id.menu_single_selection_group, (selectedCount == 1));
        if(mMode == Mode.Notify) {

        }
    }

    // TODO Do in background & add confirm dialog
    private void doDelete(long[] _ids) {
        for(int i = 0; i < _ids.length; i++) {
            long memberId = _ids[i];
            mDb.delete(memberId, Member.class);
        }
        Toast.makeText(getActivity(), _ids.length + " deleted", Toast.LENGTH_SHORT).show();
    }

    private void doNotify(Group _group) {
        DrawResult dr = mDb.queryLatestDrawResultForGroup(_group.getId());
        if(dr != null)
            mDrawManager.onNotifyDraw(dr);
    }

    private void doDraw(Group _group) {
        mDrawManager.onRequestDraw(_group);
    }

    private void doRestrictions(long _groupId, long _id) {
        mDrawManager.onRestrictMember(_groupId, _id);
    }

    // TODO Make this load asynchronously and somewhere else
    private void loadMembers(long _groupId) {
        List<Member> members = mDb.queryAllMembersForGroup(_groupId);
        Log.i(TAG, "loadMembers() - retrieved members count: " + members.size());
        setListAdapter(new MemberListAdapter(getActivity(), R.layout.member_row, members));
    }

    public long getGroupId() {
        return mGroup.getId();
    }

    private void addMember(Member _member, Group _group) {
        _member.setGroup(_group);
        long id = mDb.create(_member);
        if(id != PersistableObject.UNSET_ID) {
            loadMembers(_group.getId());
        }
    }

    public void onDrawAvailable(DrawResult drawResult) {
        mMode = Mode.Notify;
        // TODO Show Notify button
    }

    public void onDrawCleared() {
        // Revert to building mode
        mMode = Mode.Building;
        // TODO Hide Notify button
    }
}
