package com.moac.android.opensecretsanta.notify;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import com.google.common.base.Strings;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.activity.Intents;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Member;

import java.util.ArrayList;

public class SmsNotifier implements Notifier {

    private static final String TAG = SmsNotifier.class.getSimpleName();
    private static final String SENT_SMS_ACTION = "com.moac.android.opensecretsanta.SENT_SMS_ACTION";

    private final Context mContext;
    private final SmsManager mSmsManager;
    private final boolean mIsMultipartSupported;

    public SmsNotifier(Context _context, SmsManager _smsManager, boolean _isMultipartSupported) {
        mContext = _context.getApplicationContext();
        mSmsManager = _smsManager;
        mIsMultipartSupported = _isMultipartSupported;
    }

    @Override
    public void notify(Assignment _assignment, Member _giver, String _receiverName, String _groupMsg) {
        Log.i(TAG, "notify() - SMS. giver:" + _giver + " receiverName:" + _receiverName + "groupMsg: " + _groupMsg);
        String phoneNumber = _giver.getContactDetails();
        String msg = buildMsg(mContext.getString(R.string.sms_assignment_msg), _groupMsg, _giver.getName(), _receiverName);

        // Split long messages
        ArrayList<String> messages = mSmsManager.divideMessage(msg);
        Log.v(TAG, "notify() - divided into: " + messages.size());

        // Sent SMS receiver is register in manifest
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        sentIntent.putExtra(Intents.ASSIGNMENT_ID_INTENT_EXTRA, _assignment.getId());
        PendingIntent sentPI = PendingIntent.getBroadcast(mContext, 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(mIsMultipartSupported) {
            Log.v(TAG, "notify() - sending multipart message: " + messages.size());
            // Build the multipart SMS before sending.
            ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
            for(String part : messages) {
                sentIntents.add(sentPI); // one per part
            }
            mSmsManager.sendMultipartTextMessage(phoneNumber, null, messages, sentIntents, null);
        } else {
            Log.v(TAG, "notify() - sending multiple single messages: " + messages.size());
            // Just iterate manually.
            for(String partialMsg : messages) {
                mSmsManager.sendTextMessage(phoneNumber, null, partialMsg, sentPI, null);
            }
        }

        /*
         * This is a hack to lower the occurrences of the SmsManager reporting RESULT_ERROR_LIMIT_EXCEEDED
         * and thus failing to send the SMS
         *
         * Even when sending relatively low numbers of SMS in a short timeframe the SmsManager
         * can be overwhelmed and being reporting failures indicating that its queue is full.
         *
         * This queue is an internal mechanism so there's not a lot that can be done other than
         * backoff the sending a little. A cheap and nasty way to do this is to always introduce
         * a delay when sending messages. A smarter way would involve only backing off once the
         * messages began to fail and then managing the resending of them.
         *
         * This is the cheap and nasty approach. It means that this notifier will take longer
         * than it should to run.
         *
         */
        Log.v(TAG, "notify() - backing off");
        try {
            Thread.sleep(750);
        } catch(InterruptedException e) {
        }
        Log.v(TAG, "notify() - end");
    }

    /**
     * Generate a concise notification message to send via SMS
     */
    private static String buildMsg(String _baseMsg, String _groupMsg, String _giverName, String _receiverName) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(_baseMsg, _giverName, _receiverName));
        sb.append(Strings.isNullOrEmpty(_groupMsg) ? "" : " " + _groupMsg);
        Log.v(TAG, "buildMsg() - result: " + sb.toString());

        return sb.toString();
    }
}
