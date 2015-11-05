package com.moac.android.opensecretsanta.module;

import com.moac.android.opensecretsanta.inject.base.ForApplication;

import android.accounts.AccountManager;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class AccountModule {

    private static final String TAG = AccountModule.class.getSimpleName();

    @Provides
    @Singleton
    AccountManager provideAccountManager(@ForApplication Context context) {
        return AccountManager.get(context);
    }

}