package com.moac.android.opensecretsanta.notify.sms;

import android.content.Context;
import android.util.Log;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.notify.Notifier;
import com.moac.android.opensecretsanta.util.TextUtils;

public class SmsNotifier implements Notifier {

    private static final String TAG = SmsNotifier.class.getSimpleName();

    private final Context mContext;
    private final SmsTransporter mSmsTransporter;

    public SmsNotifier(Context _context, SmsTransporter _smsTransporter) {
        mContext = _context.getApplicationContext();
        mSmsTransporter = _smsTransporter;
    }

    @Override
    public void notify(Assignment _assignment, Member _giver, String _receiverName, String _groupMsg) {
        String msg = buildSmsMsg(mContext.getString(R.string.sms_assignment_msg_unformatted), _groupMsg, _giver.getName(), _receiverName);
        mSmsTransporter.send(_assignment, _giver.getContactDetails(), msg);
    }

    /**
     * Generate a concise notification message to send via SMS
     */
    private static String buildSmsMsg(String _baseMsg, String _groupMsg, String _giverName, String _receiverName) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(_baseMsg, _giverName, _receiverName));
        sb.append(TextUtils.isNullOrEmpty(_groupMsg) ? "" : " " + _groupMsg);
        Log.v(TAG, "buildSmsMsg() - result: " + sb.toString());
        return sb.toString();
    }
}
