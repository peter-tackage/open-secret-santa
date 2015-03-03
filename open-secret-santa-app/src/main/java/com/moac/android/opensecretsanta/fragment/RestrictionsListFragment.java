package com.moac.android.opensecretsanta.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.moac.android.inject.dagger.InjectingListFragment;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.activity.Intents;
import com.moac.android.opensecretsanta.adapter.RestrictionListAdapter;
import com.moac.android.opensecretsanta.adapter.RestrictionViewModel;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.Restriction;
import com.moac.android.opensecretsanta.viewmodel.RestrictionsViewModelProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.app.AppObservable;
import rx.functions.Action1;

public class RestrictionsListFragment extends InjectingListFragment implements Saveable {

    private static final String TAG = RestrictionsListFragment.class.getSimpleName();

    // Restriction change actions
    private enum Action {
        Create, Delete
    }

    @Inject
    DatabaseManager mDb;

    @Inject
    RestrictionsViewModelProvider mViewModelProvider;

    private Member mMember;
    private long mGroupId;
    private Map<Long, Action> mChanges;

    private Subscription mSubscription;

    public static RestrictionsListFragment create(long groupId, long memberId) {
        Log.i(TAG, "RestrictionsListFragment() - factory creating for groupId: " + groupId + " memberId: " + memberId);
        RestrictionsListFragment fragment = new RestrictionsListFragment();
        Bundle args = new Bundle();
        args.putLong(Intents.GROUP_ID_INTENT_EXTRA, groupId);
        args.putLong(Intents.MEMBER_ID_INTENT_EXTRA, memberId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mGroupId = getArguments().getLong(Intents.GROUP_ID_INTENT_EXTRA);
        long memberId = getArguments().getLong(Intents.MEMBER_ID_INTENT_EXTRA);
        mMember = mDb.queryById(memberId, Member.class);
        mChanges = new HashMap<>();

        TextView titleTextView = (TextView) getView().findViewById(R.id.textView_groupName);
        titleTextView.setText(String.format(getString(R.string.restriction_list_title_unformatted), mMember.getName()));

    }

    @Override
    public void onResume() {
        super.onResume();
        mSubscription = AppObservable.bindFragment(this, mViewModelProvider
                .getRestrictionViewModel(mGroupId, mMember.getId()))
                .subscribe(new Action1<List<RestrictionViewModel>>() {
                    @Override
                    public void call(List<RestrictionViewModel> restrictionViewModelList) {
                        ListAdapter adapter = new RestrictionListAdapter(getActivity(), restrictionViewModelList, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                RestrictionViewModel viewModel = (RestrictionViewModel) v.getTag();
                                handleRestrictionToggle(viewModel);
                            }
                        });
                        setListAdapter(adapter);
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        mSubscription.unsubscribe();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restrictions_list, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    /**
     * Keeps track of changes made to the restrictions without
     * writing to the database.
     */
    private void handleRestrictionToggle(RestrictionViewModel viewModel) {
        Log.d(TAG, "handleRestrictionToggle() - start");
        long id = viewModel.getToMemberId();
        if (mChanges.containsKey(id)) {
            Log.v(TAG, "handleRestrictionToggle() - unmarking change");
            mChanges.remove(id);
        } else {
            Log.v(TAG, "handleRestrictionToggle() - marking change");
            Action action = !viewModel.isRestricted() ? Action.Create : Action.Delete;
            mChanges.put(id, action);
        }
        Log.d(TAG, "handleRestrictionToggle() - change list:" + mChanges);
    }


    @Override
    public boolean save() {
        boolean isDirty = !mChanges.isEmpty();
        Log.i(TAG, "doSaveAction() - isDirty: " + isDirty);

        // TODO Make this a background task
        if (isDirty) {
            Log.i(TAG, "doSaveAction() - Restrictions have changed: deleting existing assignments");
            // Restrictions have changed - invalidate the draw.
            mDb.deleteAllAssignmentsForGroup(mGroupId);

            for (Map.Entry<Long, Action> entry : mChanges.entrySet()) {
                Log.d(TAG, "doSaveAction() - Change Entry: " + entry);
                if (entry.getValue().equals(Action.Create)) {
                    Log.i(TAG, "doSaveAction() - Adding new Restriction");
                    // Add a new Restriction
                    Restriction restriction = new Restriction();
                    restriction.setMember(mMember);
                    restriction.setOtherMemberId(entry.getKey());
                    mDb.create(restriction);
                } else {
                    // Delete restriction
                    Log.i(TAG, "doSaveAction() - Deleting Restriction");
                    mDb.deleteRestrictionBetweenMembers(mMember.getId(), entry.getKey());
                }
            }
        }
        // Save is always valid
        return true;
    }
}
