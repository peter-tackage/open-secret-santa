package com.moac.android.opensecretsanta.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.activity.Intents;
import com.moac.android.opensecretsanta.activity.OnMemberClickListener;
import com.moac.android.opensecretsanta.adapter.MemberRowDetails;
import com.moac.android.opensecretsanta.adapter.MemberListAdapter;
import com.moac.android.opensecretsanta.adapter.SuggestionsAdapter;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.PersistableObject;
import com.moac.android.opensecretsanta.model.Restriction;

import java.util.ArrayList;
import java.util.List;

public class MemberListFragment extends ListFragment {

    private static final String TAG = MemberListFragment.class.getSimpleName();

    private Group mGroup;
    private DatabaseManager mDb;
    private OnMemberClickListener mOnMemberClickListener;
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
            mOnMemberClickListener = (OnMemberClickListener) _activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(_activity.toString() + " must implement OnMemberClickListener");
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

        mCompleteTextView = (AutoCompleteTextView)view.findViewById(R.id.add_autoCompleteTextView);
        mCompleteTextView.setThreshold(1);
        mCompleteTextView.setAdapter(new SuggestionsAdapter(getActivity()));
        mCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Member selected = (Member)mCompleteTextView.getAdapter().getItem(position);
                Log.i(TAG, "OnItemClick() - name: " + selected.getName());
                addMember(selected);
                mCompleteTextView.setText("");
                mCompleteTextView.requestFocus(); // Keep focus for more entries
                Toast.makeText(getActivity(), selected.getName() + " added", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onActivityCreated() - loading members for groupId: " + mGroup.getId());
        loadMembers();
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Member row = (Member)getListView().getAdapter().getItem(position);
                mOnMemberClickListener.onMemberClick(mGroup.getId(), row.getId());
            }
        });
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
        if (id != PersistableObject.UNSET_ID) {
            loadMembers();
        }
    }

}
