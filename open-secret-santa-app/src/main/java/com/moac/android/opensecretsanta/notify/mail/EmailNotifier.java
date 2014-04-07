package com.moac.android.opensecretsanta.notify.mail;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.google.common.base.Strings;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.notify.NotificationFailureException;
import com.moac.android.opensecretsanta.notify.Notifier;
import com.moac.android.opensecretsanta.notify.NotifyStatusEvent;
import com.squareup.otto.Bus;

public class EmailNotifier implements Notifier {

    private static final String TAG = EmailNotifier.class.getSimpleName();

    private final Context mContext;
    private final Bus mBus;
    private final DatabaseManager mDb;
    private final EmailTransporter mEmailTransporter;
    private final String mSenderAddress; // The device owner's address!
    private final String mToken;
    private final Handler mHandler;

    public EmailNotifier(Context context, Bus bus, DatabaseManager db, Handler handler,
                         EmailTransporter sender, String senderAddress, String token) {
        mContext = context;
        mBus = bus;
        mDb = db;
        mHandler = handler;
        mEmailTransporter = sender;
        mSenderAddress = senderAddress;
        mToken = token;
    }

    @Override
    public void notify(final Assignment _assignment, Member _giver, String _receiverName, String _groupMsg) {
        String body = buildEmailMsg(mContext.getString(R.string.email_assignment_msg), _giver.getName(),
                _receiverName, _groupMsg, mContext.getString(R.string.email_footer_msg));
        try {
            mEmailTransporter.send(mContext.getString(R.string.email_subject_msg), body, mSenderAddress,
                    mToken, _giver.getContactDetails());
            _assignment.setSendStatus(Assignment.Status.Sent);
        } catch(NotificationFailureException e) {
            Log.e(TAG, "Exception when sending email to: " + _giver.getContactDetails(), e);
            _assignment.setSendStatus(Assignment.Status.Failed);
        }
        // Persist the result
        mDb.update(_assignment);
        // Notify via bus
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(new NotifyStatusEvent(_assignment));
            }
        });
    }

    /**
     * Generate a verbose notification message to send via Email
     */
    private static String buildEmailMsg(String _baseMsg, String _giverName, String _receiverName, String _groupMsg, String _footer) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(_baseMsg, _giverName, _receiverName));
        sb.append(Strings.isNullOrEmpty(_groupMsg) ? "" : " " +  _groupMsg);
        sb.append(_footer);
        Log.v(TAG, "buildEmailMsg() - result: " + sb.toString());

        return sb.toString();
    }
}
