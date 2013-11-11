package com.moac.android.opensecretsanta.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.content.BusProvider;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.notify.EmailNotifier;
import com.moac.android.opensecretsanta.notify.Notifier;
import com.moac.android.opensecretsanta.notify.NotifyExecutor;
import com.moac.android.opensecretsanta.notify.SmsNotifier;
import com.moac.android.opensecretsanta.notify.mail.GmailOAuth2Sender;
import com.moac.android.opensecretsanta.notify.receiver.SmsSendReceiver;
import com.squareup.otto.Bus;

import java.util.List;

public class NotifyExecutorFragment extends Fragment implements NotifyExecutor {

    // TODO Inject
    DatabaseManager mDb;
    Bus mBus;

    private static final String TAG = NotifyExecutorFragment.class.getSimpleName();

    public static NotifyExecutorFragment create() {
        NotifyExecutorFragment fragment = new NotifyExecutorFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDb = OpenSecretSantaApplication.getDatabase();
        mBus = BusProvider.getInstance();
    }

    @Override
    public void notifyDraw(Group group) {
        List<Member> members = mDb.queryAllMembersForGroup(group.getId());
        notifyDraw(group, members);
    }

    @Override
    public void notifyDraw(Group group, List<Member> members) {
        NotifierTask task = new NotifierTask(getActivity(), mBus, mDb, group, members);
        task.execute();
    }

    public static class NotifierTask extends AsyncTask<Void, Void, Boolean> {

        private final Bus mBus;
        private final Context mApplicationContext;
        private final DatabaseManager mDatabaseManager;
        private final Group mGroup;
        private final List<Member> mMembers;

        public NotifierTask(Context _context, Bus _bus, DatabaseManager _db, Group _group, List<Member> members) {
            mApplicationContext = _context.getApplicationContext();
            mBus = _bus;
            mDatabaseManager = _db;
            mGroup = _group;
            mMembers = members;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        private boolean executeNotify() {
            // Iterate through the provided members - get their Assignment.
            for(Member member : mMembers) {
                Assignment assignment = mDatabaseManager.queryAssignmentForMember(member.getId());
                if(assignment == null) {
                    Log.e(TAG, "executeNotify() - No Assignment for Member: " + member.getName());
                    continue;
                }
                Member giftReceiver = mDatabaseManager.queryById(assignment.getReceiverMemberId(), Member.class);

                switch(member.getContactMode()) {
                    case SMS:
                        Log.i(TAG, "executeNotify() - Building SMS Notifier for: " + member.getName());
                        SmsNotifier smsNotifier = new SmsNotifier(mApplicationContext, new SmsSendReceiver(mBus, mDatabaseManager), true);
                        smsNotifier.notify(member, giftReceiver.getName(), mGroup.getMessage());
                        break;
                    case EMAIL:
                        Log.i(TAG, "executeNotify() - Building Email Notifier for: " + member.getName());
                        GmailOAuth2Sender sender = new GmailOAuth2Sender();
                        // FIXME Use Account details
                        EmailNotifier emailNotifier = new EmailNotifier(mApplicationContext, mBus, mDatabaseManager, sender , "senderAddress@somewehre.com", "accountToken");
                        emailNotifier.notify(member, giftReceiver.getName(), mGroup.getMessage());
                        break;
                    case REVEAL_ONLY:
                        break;
                    default:
                        Log.e(TAG, "executeNotify() - Unknown contact mode: " + member.getContactMode());
                }
            }
            return true;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return executeNotify();
        }
    }
}

