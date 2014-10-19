package com.moac.android.opensecretsanta.fragment;

import android.app.ProgressDialog;
import android.util.Log;
import android.widget.Toast;

import com.moac.android.inject.dagger.InjectingFragment;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.notify.DefaultNotifyExecutor;
import com.moac.android.opensecretsanta.notify.DrawNotifier;
import com.moac.android.opensecretsanta.notify.NotifyAuthorization;
import com.moac.android.opensecretsanta.notify.NotifyStatusEvent;
import com.moac.android.opensecretsanta.notify.mail.EmailTransporter;
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

    @Inject
    DatabaseManager mDb;
    @Inject
    Bus mBus;
    @Inject
    SmsTransporter mSmsTransporter;
    @Inject
    EmailTransporter mEmailTransporter;

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
        Log.i(TAG, "Current thread is: " + Thread.currentThread().toString());
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
    }

    @Override
    public void onError(Throwable t) {
        Log.e(TAG, "onError() ", t);
        dismissProgressDialog();
        Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNext(NotifyStatusEvent event) {
        Log.i(TAG, "onNext() - got event: " + event.getAssignment());
        mBus.post(event);
    }

    @Override
    public void onDestroy() {
        if(mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
        if(mDrawProgressDialog != null && mDrawProgressDialog.isShowing()) {
            dismissProgressDialog();
        }
        super.onDestroy();
    }

    private void showDrawProgressDialog() {
        Log.v(TAG, "showDrawProgressDialog() - start");
        if(mDrawProgressDialog != null && mDrawProgressDialog.isShowing()) return;
        mDrawProgressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.notify_in_progress_msg), true);
    }

    private void dismissProgressDialog() {
        Log.v(TAG, "dismissProgressDialog() - start");
        mDrawProgressDialog.dismiss();
        mDrawProgressDialog = null;
    }
}

