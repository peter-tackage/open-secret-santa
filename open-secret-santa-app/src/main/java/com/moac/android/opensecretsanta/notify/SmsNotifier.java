package com.moac.android.opensecretsanta.notify;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.activity.Intents;
import com.moac.android.opensecretsanta.model.Member;

import java.util.ArrayList;

public class SmsNotifier implements Notifier {

    private static final String TAG = SmsNotifier.class.getSimpleName();
    private static final String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    private static final String DELIVER_SMS_ACTION = "DELIVER_SMS_ACTION";

    private final Context mContext;
    private final boolean mMultipart;
    private final BroadcastReceiver mReceiver;

    public SmsNotifier(Context _context, BroadcastReceiver _receiver, boolean _multipart) {
        mContext = _context.getApplicationContext();
        mReceiver = _receiver;
        mMultipart = _multipart;
    }

    @Override
    public void notify(Member _giver, String _receiverName, String _groupMsg) {

        String phoneNumber = _giver.getContactAddress();
        String msg = buildMsg(mContext.getString(R.string.standard_assignment_msg), _groupMsg, _giver.getName(), _receiverName);

        SmsManager smsManager = SmsManager.getDefault();

        // Split long messages
        ArrayList<String> messages = smsManager.divideMessage(msg);
        Log.v(TAG, "notify() - divided into: " + messages.size());

        // Sent receiver.
        mContext.registerReceiver(mReceiver, new IntentFilter(SENT_SMS_ACTION));
        // TODO Investigate the reliability of the Delivery vs Sent Receiver - may need interim state?
        // Delivery receiver
        //mContext.registerReceiver(new DeliveryReceiver(), new IntentFilter(DELIVER_SMS_ACTION));

        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        sentIntent.putExtra(Intents.MEMBER_ID_INTENT_EXTRA, _giver.getId());
        PendingIntent sentPI = PendingIntent.getBroadcast(mContext, 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(mMultipart) {
            Log.v(TAG, "notify() - sending multipart message: " + messages.size());
            // Build the multipart SMS before sending.
            ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
            for(String part : messages) {
                sentIntents.add(sentPI); // one per part
            }
            smsManager.sendMultipartTextMessage(phoneNumber, null, messages, sentIntents, null);
        } else {
            Log.v(TAG, "notify() - sending multiple single messages: " + messages.size());
            // Just iterate manually.
            for(String partialMsg : messages) {
                smsManager.sendTextMessage(phoneNumber, null, partialMsg, sentPI, null);
            }
        }
        Log.v(TAG, "notify() - end");
    }

    /**
     * Generate a concise notification message to send via SMS
     */
    public static String buildMsg(String _baseMsg, String _groupMsg, String _giverName, String _receiverName) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(_baseMsg, _giverName, _receiverName));
        sb.append(_groupMsg == null ? "" : _groupMsg);

        Log.v(TAG, "buildMsg() - result: " + sb.toString());

        return sb.toString();
    }
}
