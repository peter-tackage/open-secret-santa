package com.moac.android.opensecretsanta.viewmodel;

import com.moac.android.opensecretsanta.adapter.RestrictionViewModel;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.Restriction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.schedulers.Schedulers;

@Singleton
public class RestrictionsViewModelProvider {

    private final DatabaseManager mDb;

    @Inject
    public RestrictionsViewModelProvider(DatabaseManager databaseManager) {
        mDb = databaseManager;
    }

    public Observable<List<RestrictionViewModel>> getRestrictionViewModel(long groupId, long memberId) {
        List<Member> otherMembers = mDb.queryAllMembersForGroupExcept(groupId, memberId);
        List<Restriction> restrictionsForMember = mDb.queryAllRestrictionsForMemberId(memberId);
        Set<Long> restrictions = buildRestrictedMembers(restrictionsForMember);
        return Observable
                .just(buildViewModel(memberId, otherMembers, restrictions))
                .subscribeOn(Schedulers.computation());
        // TODO This is a combination of io and computation
    }

    // Bit clunky, but probably better than iterating through the List<Restriction> multiple times.
    private static Set<Long> buildRestrictedMembers(List<Restriction> _restrictions) {
        Set<Long> result = new HashSet<Long>();
        for (Restriction restriction : _restrictions) {
            result.add(restriction.getOtherMemberId());
        }
        return result;
    }

    private static List<RestrictionViewModel> buildViewModel(long fromMemberId, List<Member> otherMembers, Set<Long> restrictions) {
        List<RestrictionViewModel> viewModels = new ArrayList<>(otherMembers.size());
        for (Member other : otherMembers) {
            RestrictionViewModel rowDetails = new RestrictionViewModel(
                    fromMemberId,
                    other.getId(),
                    other.getName(),
                    restrictions.contains(other.getId()),
                    other.getContactId(),
                    other.getLookupKey());
            viewModels.add(rowDetails);
        }
        return viewModels;
    }
}
