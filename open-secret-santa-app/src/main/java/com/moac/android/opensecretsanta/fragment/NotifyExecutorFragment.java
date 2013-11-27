package com.moac.android.opensecretsanta.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.content.BusProvider;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.notify.DefaultNotifyExecutor;
import com.moac.android.opensecretsanta.notify.DrawNotifier;
import com.moac.android.opensecretsanta.notify.NotifyAuthorization;
import com.moac.android.opensecretsanta.notify.NotifyStatusEvent;
import com.squareup.otto.Bus;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.concurrency.AndroidSchedulers;
import rx.concurrency.Schedulers;

public class NotifyExecutorFragment extends Fragment implements DrawNotifier, Observer<NotifyStatusEvent> {

    private static final String TAG = NotifyExecutorFragment.class.getSimpleName();

    // TODO Inject
    // TODO We don't handle multiple requests at once
    DatabaseManager mDb;
    Bus mBus;
    private ProgressDialog mDrawProgressDialog;
    private Subscription mSubscription;

    public static NotifyExecutorFragment create() {
        NotifyExecutorFragment fragment = new NotifyExecutorFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDb = OpenSecretSantaApplication.getInstance().getDatabase();
        mBus = BusProvider.getInstance();
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
        DefaultNotifyExecutor executor = new DefaultNotifyExecutor(getActivity(), auth, mDb, mBus);
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
        }
        if(mDrawProgressDialog != null && mDrawProgressDialog.isShowing()) {
            dismissProgressDialog();
        }
        super.onDestroy();
    }

    private void showDrawProgressDialog() {
        Log.v(TAG, "showDrawProgressDialog() - start");
        if(mDrawProgressDialog != null && mDrawProgressDialog.isShowing())
            return;
        mDrawProgressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.notify_in_progress_msg), true);
    }

    private void dismissProgressDialog() {
        Log.v(TAG, "dismissProgressDialog() - start");
        mDrawProgressDialog.dismiss();
        mDrawProgressDialog = null;
    }
}

