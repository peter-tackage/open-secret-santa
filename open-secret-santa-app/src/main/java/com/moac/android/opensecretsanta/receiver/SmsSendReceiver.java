package com.moac.android.opensecretsanta.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.moac.android.opensecretsanta.activity.Intents;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.PersistableObject;

/**
 * A Receiver will get all events that match the IntentFilter, so we can't
 * initialise it with an Assignment and assume that the onReceive callback is
 * for that Assignment. receipt. Instead we use the Extra in the Intent
 * to determine which Assignment is being processed.
 */
public class SmsSendReceiver extends BroadcastReceiver {

    private static final String TAG = SmsSendReceiver.class.getSimpleName();

    private final DatabaseManager mDb;

    public SmsSendReceiver(DatabaseManager _db) {
        mDb = _db;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            long memberId = intent.getLongExtra(Intents.MEMBER_ID_INTENT_EXTRA, PersistableObject.UNSET_ID);

            /**
             * Due to the asynchronous nature of this callback, we need to be defensive.
             */

            if(memberId == PersistableObject.UNSET_ID) {
                Log.e(TAG, "onReceive() - Member Id extra not set");
                return;
            }

            Assignment assignment = mDb.queryAssignmentForMember(memberId);
            if(assignment == null) {
                Log.e(TAG, "onReceive() - No Assignment for Member Id: " + memberId);
                return;
            }

            Log.i(TAG, "onReceive() got message sent notification:" + intent);
            switch(getResultCode()) {
                case Activity.RESULT_OK:
                    Log.i(TAG, "onReceive() - Success sending SMS");
                    assignment.setSendStatus(Assignment.Status.Sent);
                    break;
                default:
                    Log.i(TAG, "onReceive() - Failure sending SMS");
                    assignment.setSendStatus(Assignment.Status.Failed);
            }
            // Update Assignment with Sent Statua
            mDb.update(assignment);
        } finally {
            // Register ourselves.
            context.unregisterReceiver(this);
        }
    }
}
