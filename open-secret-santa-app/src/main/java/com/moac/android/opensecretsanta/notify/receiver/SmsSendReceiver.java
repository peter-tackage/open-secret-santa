package com.moac.android.opensecretsanta.notify.receiver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.moac.android.inject.dagger.InjectingBroadcastReceiver;
import com.moac.android.opensecretsanta.activity.Intents;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.PersistableObject;
import com.moac.android.opensecretsanta.notify.NotifyStatusEvent;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * A Receiver will get all events that match the IntentFilter, so we can't
 * initialise it with an Assignment and assume that the onReceive callback is
 * for that Assignment receipt. Instead we use the Extra in the Intent
 * to determine which Assignment is being processed.
 */
public class SmsSendReceiver extends InjectingBroadcastReceiver {

    private static final String TAG = SmsSendReceiver.class.getSimpleName();

    @Inject
    DatabaseManager mDb;

    @Inject
    Bus mBus;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        /**
         * Due to the asynchronous nature of this callback, we need to be defensive.
         * The entities that were initiated the events may have changed or
         * even have been removed.
         *
         * Note: This callback happens on the main thread
         */

        long assignmentId = intent.getLongExtra(Intents.ASSIGNMENT_ID_INTENT_EXTRA, PersistableObject.UNSET_ID);

        if(assignmentId <= PersistableObject.UNSET_ID) {
            Log.e(TAG, "onReceive() - Assignment Id extra not set");
            return;
        }

        Assignment assignment = mDb.queryById(assignmentId, Assignment.class);
        if(assignment == null) {
            Log.e(TAG, "onReceive() - No Assignment found to update, id: " + assignmentId);
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
