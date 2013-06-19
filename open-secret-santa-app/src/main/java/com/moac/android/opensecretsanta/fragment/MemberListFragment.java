package com.moac.android.opensecretsanta.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.activity.Intents;
import com.moac.android.opensecretsanta.activity.OnMemberClickListener;
import com.moac.android.opensecretsanta.adapter.MemberListAdapter;
import com.moac.android.opensecretsanta.adapter.MemberRowDetails;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.Restriction;

import java.util.ArrayList;
import java.util.List;

public class MemberListFragment extends ListFragment {

    private static final String TAG = MemberListFragment.class.getSimpleName();

    /**
     * The fragment's associated Group
     */
    private long mGroupId;

    // Shorthand to database.
    private DatabaseManager mDb;

    private OnMemberClickListener mOnMemberClickListener;

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
        mGroupId = getArguments().getLong(Intents.GROUP_ID_INTENT_EXTRA);
    }

    @Override
    public void onStart() {
        super.onStart();
        // TODO Make this load asynchronously and somewhere else
        Log.i(TAG, "onActivityCreated() - loading members for groupId: " + mGroupId);
        List<Member> members = mDb.queryAllMembersForGroup(mGroupId);
        List<MemberRowDetails> items = buildRowData(members);
        Log.i(TAG, "onActivityCreated() - retrieved members count: " + items.size());
        setListAdapter(new MemberListAdapter(getActivity(), R.layout.member_row, items));
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MemberRowDetails row = (MemberRowDetails)getListView().getAdapter().getItem(position);
                mOnMemberClickListener.onMemberClick(mGroupId, row.getMemberId());
            }
        });
    }

    public long getGroupId() {
        return mGroupId;
    }

    private List<MemberRowDetails> buildRowData(List<Member> _members) {
        List<MemberRowDetails> rows = new ArrayList<MemberRowDetails>(_members.size());
        for(Member member : _members) {
            long memberId = member.getId();
            List<Restriction> restrictions = mDb.queryAllRestrictionsForMemberId(memberId);
            MemberRowDetails row = new MemberRowDetails(memberId, member.getLookupKey(),
              member.getName(), member.getContactMode(), member.getContactAddress(), restrictions.size());
            rows.add(row);
        }
        return rows;
    }
}
