package com.moac.android.opensecretsanta.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.activity.Intents;
import com.moac.android.opensecretsanta.adapter.RestrictionListAdapter;
import com.moac.android.opensecretsanta.adapter.RestrictionRowDetails;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.PersistableObject;
import com.moac.android.opensecretsanta.model.Restriction;

import java.util.*;

public class RestrictionsListFragment extends ListFragment {

    private static final String TAG = RestrictionsListFragment.class.getSimpleName();

    private DatabaseManager mDb;
    private long mFromMemberId = PersistableObject.UNSET_ID;
    private long mGroupId = PersistableObject.UNSET_ID;

    /**
     * Factory method for this fragment class
     */
    public static RestrictionsListFragment create(long _groupId, long _fromMemberId) {
        Log.i(TAG, "RestrictionsListFragment() - factory creating for groupId: " + _groupId + " fromMemberId: " + _fromMemberId);
        RestrictionsListFragment fragment = new RestrictionsListFragment();
        Bundle args = new Bundle();
        args.putLong(Intents.GROUP_ID_INTENT_EXTRA, _groupId);
        args.putLong(Intents.MEMBER_ID_INTENT_EXTRA, _fromMemberId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDb = OpenSecretSantaApplication.getDatabase();
        mGroupId = getArguments().getLong(Intents.GROUP_ID_INTENT_EXTRA);
        mFromMemberId = getArguments().getLong(Intents.MEMBER_ID_INTENT_EXTRA);
    }

    @Override
    public void onStart() {
        super.onStart();
        // TODO Make this load asynchronously and somewhere else
        Log.i(TAG, "onStart() - loading members for groupId: " + mGroupId);
        List<Member> otherMembers = mDb.queryAllMembersForGroupExcept(mGroupId, mFromMemberId);
        Log.i(TAG, "onStart() - loaded member count: " + otherMembers.size());
        List<Restriction> restrictionsForMember = mDb.queryAllRestrictionsForMemberId(mFromMemberId);
        Log.i(TAG, "onStart() - loaded restrictions count: " + restrictionsForMember.size());
        Set<Long> restrictionSet = buildRestrictionSet(restrictionsForMember);
        Log.i(TAG, "onStart() - built restriction set count: " + restrictionSet.size());
        List<RestrictionRowDetails> rows = buildRowData(mFromMemberId, otherMembers, restrictionSet);
        Log.i(TAG, "onStart() - built row data: " + rows.size());
        setListAdapter(new RestrictionListAdapter(getActivity(), rows));
    }

    private static List<RestrictionRowDetails> buildRowData(long _fromMemberId, List<Member> _otherMembers, Set<Long> _restrictions) {
        List<RestrictionRowDetails> rows = new ArrayList<RestrictionRowDetails>(_otherMembers.size());
        for(Member other: _otherMembers) {
            RestrictionRowDetails rowDetails = new RestrictionRowDetails();
            rowDetails.setFromMemberId(_fromMemberId);
            rowDetails.setToMemberId(other.getId());
            rowDetails.setRestricted(_restrictions.contains(other.getId()));
            rowDetails.setToMemberName(other.getName());
            rows.add(rowDetails);
        }
        return rows;
    }

    // Bit clunky, but probably better than iterating through the List<Restriction> multiple times.
    private static Set<Long> buildRestrictionSet(List<Restriction> _restrictions) {
        Set<Long> result = new HashSet<Long>();
        for(Restriction restriction : _restrictions) {
           result.add(restriction.getId());
        }
        return result;
    }

    public boolean doSaveAction() {
        Log.i(TAG, "doSaveAction() called");
        return true;
    }
}
