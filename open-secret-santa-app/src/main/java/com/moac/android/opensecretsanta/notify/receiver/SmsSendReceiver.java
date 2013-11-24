package com.moac.android.opensecretsanta.notify.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.activity.Intents;
import com.moac.android.opensecretsanta.content.BusProvider;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.PersistableObject;
import com.moac.android.opensecretsanta.notify.NotifyStatusEvent;
import com.squareup.otto.Bus;

/**
 * A Receiver will get all events that match the IntentFilter, so we can't
 * initialise it with an Assignment and assume that the onReceive callback is
 * for that Assignment. receipt. Instead we use the Extra in the Intent
 * to determine which Assignment is being processed.
 */
public class SmsSendReceiver extends BroadcastReceiver {

    private static final String TAG = SmsSendReceiver.class.getSimpleName();

    // TODO Inject these
    private final DatabaseManager mDb;
    private final Bus mBus;

    // Registering in the manifest requires a default constructor
    public SmsSendReceiver() {
        mBus = BusProvider.getInstance();
        mDb = OpenSecretSantaApplication.getInstance().getDatabase();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        /**
         * Due to the asynchronous nature of this callback, we need to be defensive.
         * The entities that were initiated the events may have changed or
         * even have been removed.
         *
         * This callback does happen on the main thread, though.
         */

        long memberId = intent.getLongExtra(Intents.MEMBER_ID_INTENT_EXTRA, PersistableObject.UNSET_ID);

        if(memberId <= PersistableObject.UNSET_ID) {
            Log.e(TAG, "onReceive() - Member Id extra not set");
            return;
        }

        Assignment assignment = mDb.queryAssignmentForMember(memberId);
        if(assignment == null) {
            Log.e(TAG, "onReceive() - No Assignment to update for Member Id: " + memberId);
            return;
        }

        Log.i(TAG, "onReceive() got message sent notification:" + intent);
        switch(getResultCode()) {
            case Activity.RESULT_OK:
                Log.i(TAG, "onReceive() - Success sending SMS");
                assignment.setSendStatus(Assignment.Status.Sent);
                break;
            default:
                Log.i(TAG, "onReceive() - Failure sending SMS: code - " + getResultCode() + " data: " + getResultData());
                assignment.setSendStatus(Assignment.Status.Failed);
        }
        Log.d(TAG, "OnReceive() - updating Assignment and posting to bus: " + assignment);
        // Update Assignment with Sent Status
        mDb.update(assignment);
        // Post update of Assignment status to Bus.
        mBus.post(new NotifyStatusEvent(assignment));
    }
}
