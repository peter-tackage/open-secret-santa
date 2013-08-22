package com.moac.android.opensecretsanta.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.activity.Intents;
import com.moac.android.opensecretsanta.adapter.RestrictionListAdapter;
import com.moac.android.opensecretsanta.adapter.RestrictionRowDetails;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.Restriction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RestrictionsListFragment extends ListFragment {

    private static final String TAG = RestrictionsListFragment.class.getSimpleName();

    private DatabaseManager mDb;
    private Member mFromMember;
    private Group mGroup;
    private ListAdapter mAdapter;
    List<Restriction> mInitialRestrictionsForMember;

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
        setRetainInstance(true);

        mDb = OpenSecretSantaApplication.getDatabase();
        long groupId = getArguments().getLong(Intents.GROUP_ID_INTENT_EXTRA);
        mGroup = mDb.queryById(groupId, Group.class);
        long memberId = getArguments().getLong(Intents.MEMBER_ID_INTENT_EXTRA);
        mFromMember = mDb.queryById(memberId, Member.class);

        // TODO Make this load asynchronously
        long fromMemberId = mFromMember.getId();
        List<Member> otherMembers = mDb.queryAllMembersForGroupExcept(mGroup.getId(), fromMemberId);
        mInitialRestrictionsForMember = mDb.queryAllRestrictionsForMemberId(fromMemberId);
        Set<Long> restrictionSet = buildRestrictionSet(mInitialRestrictionsForMember);
        List<RestrictionRowDetails> rows = buildRowData(fromMemberId, otherMembers, restrictionSet);
        mAdapter = new RestrictionListAdapter(getActivity(), rows, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RestrictionRowDetails details = (RestrictionRowDetails)v.getTag();
                handleRestrictionToggle(details);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.restrictions_list_fragment, container, false);
        TextView titleTextView = (TextView) view.findViewById(R.id.content_title_textview);
        titleTextView.setText("Restrictions for " + mFromMember.getName());

        setListAdapter(mAdapter);

        return view;
    }

    private void handleRestrictionToggle(RestrictionRowDetails _details) {

    }

    private static List<RestrictionRowDetails> buildRowData(long _fromMemberId, List<Member> _otherMembers, Set<Long> _restrictions) {
        List<RestrictionRowDetails> rows = new ArrayList<RestrictionRowDetails>(_otherMembers.size());
        for(Member other : _otherMembers) {
            RestrictionRowDetails rowDetails = new RestrictionRowDetails();
            rowDetails.setFromMemberId(_fromMemberId);
            rowDetails.setToMemberId(other.getId());
            rowDetails.setRestricted(_restrictions.contains(other.getId()));
            rowDetails.setToMemberName(other.getName());
            rowDetails.setContactId(other.getContactId());
            rowDetails.setLookupKey(other.getLookupKey());
            rows.add(rowDetails);
        }
        return rows;
    }

    // Bit clunky, but probably better than iterating through the List<Restriction> multiple times.
    private static Set<Long> buildRestrictionSet(List<Restriction> _restrictions) {
        Set<Long> result = new HashSet<Long>();
        for(Restriction restriction : _restrictions) {
            result.add(restriction.getOtherMemberId());
        }
        return result;
    }

    public boolean doSaveAction() {
        Log.i(TAG, "doSaveAction() called");
        // Compare to Initial. If different, save, return true.
        return true;
        // if the same, return false.
    }
}
