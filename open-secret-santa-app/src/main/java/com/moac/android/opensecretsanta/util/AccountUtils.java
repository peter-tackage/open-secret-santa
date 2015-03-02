package com.moac.android.opensecretsanta.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.notify.mail.EmailAuthorization;
import com.moac.android.opensecretsanta.notify.mail.GmailTransport;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;

public class AccountUtils {

    private static final String TAG = AccountUtils.class.getSimpleName();

    // Must run on new thread
    public static Observable<EmailAuthorization> getPreferredGmailAuth(final Context context, final AccountManager accountManager, final SharedPreferences prefs, final Activity activity) {
        return Observable.create(new Observable.OnSubscribe<EmailAuthorization>() {
            @Override
            public void call(final Subscriber<? super EmailAuthorization> observer) {
                Log.i(TAG, "getPreferredGmailAuth() - start");
                // Retrieve the preferred email address
                String emailPrefKey = context.getString(R.string.gmail_account_preference);
                final String emailAddress = prefs.getString(emailPrefKey, "");
                Log.v(TAG, "getPreferredGmailAuth() - current Gmail Account preference: " + emailAddress);

                // No preference.
                if (TextUtils.isEmpty(emailAddress)) {
                    observer.onError(new Exception("No preferred Gmail account found"));
                    return;
                }

                // Retrieve all Gmail accounts (must be background)
                Account[] accounts = getAllGmailAccounts(accountManager);
                if (accounts != null && accounts.length > 0) {
                    Log.v(TAG, "getPreferredGmailAuth() - found some Gmail Accounts, size: " + accounts.length);
                    // Find the Account that matches the preference
                    for (Account acc : accounts) {
                        if (acc.name.equals(emailAddress)) {
                            Log.d(TAG, "getPreferredGmailAuth() - found matching Account - will retrieve token");
                            // Get the token - this might open the framework's auth dialog to confirm permissions.
                            String token = getGmailToken(accountManager, activity, acc);
                            Log.d(TAG, "getPreferredGmailAuth() - got token: " + token);
                            observer.onNext(new EmailAuthorization(emailAddress, token));
                            observer.onCompleted();
                            return;
                        }
                    }
                    observer.onError(new Exception("Your preferred email address no longer exists"));
                }
            }
        });
    }

    // Must run on new thread
    public static Account[] getAllGmailAccounts(AccountManager accountManager) {
        AccountManagerFuture<Account[]> accountsFuture =
                accountManager.getAccountsByTypeAndFeatures(GmailTransport.ACCOUNT_TYPE_GOOGLE,
                        GmailTransport.FEATURES_MAIL, null, null);
        try {
            return accountsFuture.getResult();
        } catch (OperationCanceledException | IOException | AuthenticatorException e) {
            Log.e(TAG, "getAllGmailAccounts() - Error when fetching accounts", e);
        }
        return null;
    }

    // Must run on new thread
    public static Observable<Account[]> getAllGmailAccountsObservable(final Context context, final AccountManager accountManager) {
        return Observable.create(new Observable.OnSubscribe<Account[]>() {
            @Override
            public void call(Subscriber<? super Account[]> observer) {
                Account[] accounts = getAllGmailAccounts(accountManager);
                if (accounts != null && accounts.length > 0) {
                    observer.onNext(accounts);
                    observer.onCompleted();
                } else {
                    observer.onError(new Exception(context.getString(R.string.no_email_warning)));
                }
            }
        });
    }

    // Must run on new thread
    public static String getGmailToken(AccountManager accountManager, Activity activity, final Account account) {
        Log.d(TAG, "getGmailToken() - start");
        AccountManagerFuture<Bundle> authTokenBundle = accountManager.
                getAuthToken(account, GmailTransport.GMAIL_TOKEN_TYPE, null, activity, null, null);
        try {
            return authTokenBundle.getResult().getString(AccountManager.KEY_AUTHTOKEN);
        } catch (OperationCanceledException | IOException | AuthenticatorException e) {
            Log.e(TAG, "getGmailToken() - Error when fetching account token", e);
        }
        return null;
    }
}
