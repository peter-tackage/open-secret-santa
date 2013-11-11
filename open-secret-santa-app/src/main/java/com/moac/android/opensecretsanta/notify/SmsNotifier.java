package com.moac.android.opensecretsanta.notify;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;
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
    public void notify(Member _giver, String _receiverName, String _customMsg) {

        String phoneNumber = _giver.getContactAddress();
        String msg = buildPersonalisedMsg(_customMsg, _giver.getName(), _receiverName);

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

    public static String buildPersonalisedMsg(String _customMsg, String _giverName, String _receiverName) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hi ");
        sb.append(_giverName);
        sb.append(", Open Secret Santa has assigned you: ");
        sb.append(_receiverName);
        sb.append(".\n");
        sb.append(_customMsg == null ? "" : _customMsg);

        Log.v(TAG, "buildPersonalisedMsg() - result: " + sb.toString());

        return sb.toString();
    }
}
