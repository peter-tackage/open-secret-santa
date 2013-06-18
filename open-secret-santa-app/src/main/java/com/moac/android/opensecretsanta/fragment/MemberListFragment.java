package com.moac.android.opensecretsanta.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.activity.Intents;
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

    // The row data, joined from multiple sources.
    private List<MemberRowDetails> mItems;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDb = OpenSecretSantaApplication.getDatabase();
        mGroupId = getArguments().getLong(Intents.GROUP_ID_INTENT_EXTRA);
    }

    @Override
    public void onStart() {
        super.onStart();
        // TODO Make this load asynchronously
        Log.i(TAG, "onActivityCreated() - loading members for groupId: " + mGroupId);
        List<Member> members = mDb.queryAllMembersForGroup(mGroupId);
        mItems = buildRowData(members);
        Log.i(TAG, "onActivityCreated() - retrieved members count: " + mItems.size());
        setListAdapter(new MemberListAdapter(getActivity(), R.layout.member_row, mItems));
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
