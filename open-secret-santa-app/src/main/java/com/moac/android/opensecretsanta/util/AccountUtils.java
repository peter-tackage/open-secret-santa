package com.moac.android.opensecretsanta.util;

import android.accounts.*;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.notify.EmailAuthorization;
import com.moac.android.opensecretsanta.notify.mail.GmailOAuth2Sender;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

import java.io.IOException;

public class AccountUtils {

    private static final String TAG = AccountUtils.class.getSimpleName();

    /*
      * Returns the authorization for the user's preferred Gmail address,
      * according to our SharedPreferences otherwise null.
      *
      * Must run on new thread
      */
    public static Observable<EmailAuthorization> getPreferedGmailAuth(final Context context, final Activity activity) {
        return Observable.create(new Observable.OnSubscribeFunc<EmailAuthorization>() {
            @Override
            public Subscription onSubscribe(final Observer<? super EmailAuthorization> observer) {
                Log.i(TAG, "getPreferedGmailAccount() - start");
                // Retrieve the preferred email address
                String emailPrefKey = context.getString(R.string.gmail_account_preference);
                final String emailAddress = PreferenceManager.getDefaultSharedPreferences(context).getString(emailPrefKey, null);
                Log.v(TAG, "getPreferedGmailAccount() - current Gmail Account preference: " + emailAddress);

                // No preference.
                if(emailAddress == null || emailAddress.isEmpty()) {
                    observer.onError(new Exception("No preferred email found"));
                    return Subscriptions.empty();
                }

                // Retrieve all Gmail accounts (must be background)
                Account[] accounts = getAllGmailAccounts(context);
                if(accounts != null && accounts.length > 0) {
                    Log.v(TAG, "getPreferedGmailAccount() - found some Gmail Accounts, size: " + accounts.length);
                    // Find the Account that matches the preference
                    for(Account acc : accounts) {
                        if(acc.name.equals(emailAddress)) {
                            // Get the token - this might open the framework's auth dialog to confirm permissions.
                            String token = getGmailToken(context, activity, acc);
                            observer.onNext(new EmailAuthorization(emailAddress, token));
                            observer.onCompleted();
                            return Subscriptions.empty();
                        }
                    }
                    observer.onError(new Exception("Preferred email address no longer exists"));
                }
                return Subscriptions.empty();
            }
        });
    }

    // Must be background call
    public static Account[] getAllGmailAccounts(Context context) {
        AccountManagerFuture<Account[]> accountsFuture =
          AccountManager.get(context).getAccountsByTypeAndFeatures(GmailOAuth2Sender.ACCOUNT_TYPE_GOOGLE,
            GmailOAuth2Sender.FEATURES_MAIL, null, null);
        try {
            return accountsFuture.getResult();
        } catch(OperationCanceledException e) {
            Log.e(TAG, "getAllGmailAccounts() - Error when fetching accounts", e);
        } catch(IOException e) {
            Log.e(TAG, "getAllGmailAccounts() - Error when fetching accounts", e);
        } catch(AuthenticatorException e) {
            Log.e(TAG, "getAllGmailAccounts() - Error when fetching accounts", e);
        }
        return null;
    }

    public static Observable<Account[]> getAllGmailAccountsObservable(final Context context) {
        return Observable.create(new Observable.OnSubscribeFunc<Account[]>() {
            @Override
            public Subscription onSubscribe(Observer<? super Account[]> observer) {
                Account[] accounts = getAllGmailAccounts(context);
                if(accounts != null && accounts.length > 0) {
                    observer.onNext(accounts);
                    observer.onCompleted();
                } else {
                    observer.onError(new Exception("No Gmail Accounts available"));
                }
                return Subscriptions.empty();
            }
        });
    }

    // Must be background
    public static String getGmailToken(Context context, Activity activity, final Account account) {
        AccountManagerFuture<Bundle> authTokenBundle = AccountManager.get(context).
          getAuthToken(account, GmailOAuth2Sender.GMAIL_TOKEN_TYPE, null, activity, null, null);
        try {
            return authTokenBundle.getResult().getString(AccountManager.KEY_AUTHTOKEN);
        } catch(OperationCanceledException e) {
            Log.e(TAG, "getPreferedGmailAccount() - Error when fetching account token", e);
        } catch(IOException e) {
            Log.e(TAG, "getPreferedGmailAccount() - Error when fetching account token", e);
        } catch(AuthenticatorException e) {
            Log.e(TAG, "getPreferedGmailAccount() - Error when fetching account token", e);
        }
        return null;
    }
}
