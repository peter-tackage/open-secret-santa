package com.moac.android.opensecretsanta.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;

import com.moac.android.inject.dagger.InjectingFragment;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.notify.DefaultNotifyExecutor;
import com.moac.android.opensecretsanta.notify.DrawNotifier;
import com.moac.android.opensecretsanta.notify.NotifyAuthorization;
import com.moac.android.opensecretsanta.notify.NotifyStatusEvent;
import com.moac.android.opensecretsanta.notify.mail.EmailTransporter;
import com.moac.android.opensecretsanta.notify.sms.SmsPermissionsManager;
import com.moac.android.opensecretsanta.notify.sms.SmsTransporter;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.concurrency.AndroidSchedulers;
import rx.concurrency.Schedulers;

public class NotifyExecutorFragment extends InjectingFragment implements DrawNotifier, Observer<NotifyStatusEvent> {

    private static final String TAG = NotifyExecutorFragment.class.getSimpleName();

    private static final int RELINGUISH_SMS_PERMISSION_REQUEST_CODE = 33535;

    @Inject
    DatabaseManager mDb;
    @Inject
    Bus mBus;
    @Inject
    SmsTransporter mSmsTransporter;
    @Inject
    EmailTransporter mEmailTransporter;
    @Inject
    SmsPermissionsManager mSmsPermissionsManager;

    private ProgressDialog mDrawProgressDialog;
    private Subscription mSubscription;

    public static NotifyExecutorFragment create() {
        NotifyExecutorFragment fragment = new NotifyExecutorFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void notifyDraw(NotifyAuthorization auth, Group group, long[] memberIds) {
        // Show the progress dialog
        showDrawProgressDialog();
        // FIXME Um... what if it exists already
        mSubscription = createNotifyObservable(auth, group, memberIds)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(this);
    }

    private Observable<NotifyStatusEvent> createNotifyObservable(NotifyAuthorization auth, Group group, long[] memberIds) {
        DefaultNotifyExecutor executor = new DefaultNotifyExecutor(getActivity(), auth,
                mDb, mBus, mSmsTransporter, mEmailTransporter);
        return executor.notifyDraw(group, memberIds);
    }

    @Override
    public void onCompleted() {
        Log.i(TAG, "onCompleted");
        dismissProgressDialog();
        mSmsPermissionsManager.requestRelinquishDefaultSmsPermission(this, RELINGUISH_SMS_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onError(Throwable t) {
        Log.e(TAG, "onError() ", t);
        dismissProgressDialog();
        mSmsPermissionsManager.requestRelinquishDefaultSmsPermission(this, RELINGUISH_SMS_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onNext(NotifyStatusEvent event) {
        Log.i(TAG, "onNext() - got event: " + event.getAssignment());
        mBus.post(event);
    }

    @Override
    public void onDestroy() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
        if (mDrawProgressDialog != null && mDrawProgressDialog.isShowing()) {
            dismissProgressDialog();
        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() requestCode " + requestCode);
        // User has agreed to change back, clear the recorded value
        // This handles the situation when the user doesn't change back - they will get prompted
        // again after each notify until they agree.
        if (requestCode == RELINGUISH_SMS_PERMISSION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mSmsPermissionsManager.clearSavedDefaultSmsApp();
        }
    }

    private void showDrawProgressDialog() {
        Log.v(TAG, "showDrawProgressDialog() - start");
        if (mDrawProgressDialog != null && mDrawProgressDialog.isShowing()) return;
        mDrawProgressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.notify_in_progress_msg), true);
    }

    private void dismissProgressDialog() {
        Log.v(TAG, "dismissProgressDialog() - start");
        mDrawProgressDialog.dismiss();
        mDrawProgressDialog = null;
    }
}

