package com.moac.android.opensecretsanta.inject.base.module;

import com.moac.android.opensecretsanta.inject.base.ForActivity;
import com.moac.android.opensecretsanta.inject.base.component.ActivityScope;

import android.app.Activity;
import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class BaseActivityModule {

    private final Activity activity;

    public BaseActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    @ActivityScope
    @ForActivity
    Context provideActivityContext() {
        return activity;
    }

    @Provides
    @ActivityScope
    Activity provideActivity() {
        return activity;
    }

}
