package com.moac.android.opensecretsanta.notify;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.notify.mail.GmailOAuth2Sender;
import com.moac.android.opensecretsanta.notify.receiver.SmsSendReceiver;
import com.squareup.otto.Bus;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public class DefaultNotifyExecutor implements NotifyExecutor {

    // TODO Inject
    private final Context mContext;
    DatabaseManager mDb;
    Bus mBus;

    private static final String TAG = DefaultNotifyExecutor.class.getSimpleName();

    public DefaultNotifyExecutor(Context context, DatabaseManager db, Bus bus) {
        mContext = context;
        mDb = db;
        mBus = bus;
    }

    @Override
    public Observable<NotifyStatusEvent> notifyDraw(final Group group, final long[] memberIds) {
        return Observable.create(new Observable.OnSubscribeFunc<NotifyStatusEvent>() {
            @Override
            public Subscription onSubscribe(Observer<? super NotifyStatusEvent> observer) {
                Handler handler = new Handler(Looper.getMainLooper());
                // Iterate through the provided members - get their Assignment.
                for(long memberId : memberIds) {
                    Member member = mDb.queryById(memberId, Member.class);
                    Assignment assignment = mDb.queryAssignmentForMember(member.getId());
                    if(assignment == null) {
                        Log.e(TAG, "executeNotify() - No Assignment for Member: " + member.getName());
                        observer.onError(new Exception("No Assignment for Member: " + member.getName()));
                        observer.onCompleted(); // This is really bad!
                        return Subscriptions.empty();
                    }

                    Log.i(TAG, "executeNotify() - preparing Assignment: " + assignment);

                    Member giftReceiver = mDb.queryById(assignment.getReceiverMemberId(), Member.class);

                    switch(member.getContactMode()) {
                        case SMS:
                            Log.i(TAG, "executeNotify() - Building SMS Notifier for: " + member.getName());
                            assignment.setSendStatus(Assignment.Status.Assigned);
                            observer.onNext(new NotifyStatusEvent(assignment));
                            mDb.update(assignment);
                            SmsNotifier smsNotifier = new SmsNotifier(mContext, new SmsSendReceiver(mBus, mDb), true);
                            smsNotifier.notify(member, giftReceiver.getName(), group.getMessage());
                            break;
                        case EMAIL:
                            Log.i(TAG, "executeNotify() - Building Email Notifier for: " + member.getName());
                            assignment.setSendStatus(Assignment.Status.Assigned);
                            observer.onNext(new NotifyStatusEvent(assignment));
                            mDb.update(assignment);
                            GmailOAuth2Sender sender = new GmailOAuth2Sender();
                            // FIXME Use Account details
                            EmailNotifier emailNotifier = new EmailNotifier(mContext, mBus, mDb,
                              handler, sender, "senderAddress@somewehre.com", "accountToken");
                            emailNotifier.notify(member, giftReceiver.getName(), group.getMessage());
                            break;
                        case REVEAL_ONLY:
                            break;
                        default:
                            Log.e(TAG, "executeNotify() - Unknown contact mode: " + member.getContactMode());
                    }
                }
                observer.onCompleted();
                return Subscriptions.empty();
            }
        });
    }
}