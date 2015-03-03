package com.moac.android.opensecretsanta.viewmodel;

import com.moac.android.opensecretsanta.adapter.RestrictionViewModel;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.Restriction;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
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
        return createViewModelObservable(otherMembers, restrictionsForMember)
                .subscribeOn(Schedulers.computation());
        // TODO This is a combination of io and computation
    }

    private static Observable<List<RestrictionViewModel>> createViewModelObservable(final List<Member> otherMembers,
                                                                                    final List<Restriction> restrictions) {

        // Note: The order of the observable arguments is important.
        // If they are the other way around, then the first emits all its items before only emitting
        // the second once; we would only get one emission for the overall Observable.
        return Observable.combineLatest(
                Observable.from(restrictions)
                        .map(new Func1<Restriction, Long>() {
                            @Override
                            public Long call(Restriction restriction) {
                                return restriction.getOtherMemberId();
                            }
                        })
                        .toList(),
                Observable.from(otherMembers),
                new Func2<List<Long>, Member, RestrictionViewModel>() {
                    @Override
                    public RestrictionViewModel call(List<Long> restrictions, Member other) {
                        return new RestrictionViewModel(
                                other.getId(),
                                other.getName(),
                                restrictions.contains(other.getId()),
                                other.getContactId(),
                                other.getLookupKey());
                    }
                })
                .toSortedList();
    }
}
