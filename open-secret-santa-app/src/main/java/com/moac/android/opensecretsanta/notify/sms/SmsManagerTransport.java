package com.moac.android.opensecretsanta.notify.sms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import com.moac.android.opensecretsanta.activity.Intents;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Member;

import java.util.ArrayList;

import javax.inject.Inject;

public class SmsManagerTransport implements SmsTransporter {

    private static final String TAG = SmsManagerTransport.class.getSimpleName();
    private static final String SENT_SMS_ACTION = "com.moac.android.opensecretsanta.SENT_SMS_ACTION";

    private final Context mContext;
    private final SmsManager mSmsManager;
    private final boolean mIsMultipartSupported;

    @Inject
    public SmsManagerTransport(Context context, SmsManager smsManager, boolean isMultipartSupported) {
        mContext = context;
        mSmsManager = smsManager;
        mIsMultipartSupported = isMultipartSupported;
    }

    @Override
    public void send(Assignment assignment, String phoneNumber, String msg) {
        Log.i(TAG, "send() - SMS. msgReceiverPhoneNumber:" + phoneNumber +  "msg: " + msg);

        // Split long messages
        ArrayList<String> messages = mSmsManager.divideMessage(msg);
        Log.v(TAG, "send() - divided into: " + messages.size());

        // Sent SMS receiver is register in manifest
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        sentIntent.putExtra(Intents.ASSIGNMENT_ID_INTENT_EXTRA, assignment.getId());
        PendingIntent sentPI = PendingIntent.getBroadcast(mContext, 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (mIsMultipartSupported) {
            Log.v(TAG, "send() - sending multipart message: " + messages.size());
            // Build the multipart SMS before sending.
            ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
            for (String part : messages) {
                sentIntents.add(sentPI); // one per part
            }
            mSmsManager.sendMultipartTextMessage(phoneNumber, null, messages, sentIntents, null);
        } else {
            Log.v(TAG, "send() - sending multiple single messages: " + messages.size());
            // Just iterate manually.
            for (String partialMsg : messages) {
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
        Log.v(TAG, "send() - backing off");
        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {
            // Deliberately ignore
        }
        Log.v(TAG, "send() - end");
    }

}

