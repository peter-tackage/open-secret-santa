package com.moac.android.opensecretsanta.inject.base.module;

import com.moac.android.opensecretsanta.inject.base.ForActivity;
import com.moac.android.opensecretsanta.inject.base.component.FragmentScope;
import com.moac.android.opensecretsanta.util.Preconditions;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import dagger.Module;
import dagger.Provides;

@Module
public class BaseFragmentModule {

    private final Fragment fragment;

    public BaseFragmentModule(Fragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    @FragmentScope
    Fragment provideFragment() {
        return fragment;
    }

    @Provides
    @FragmentScope
    @ForActivity
    Activity provideActivity() {
        return Preconditions.checkNotNull(fragment.getActivity(), "Fragment is not attached to Activity");
    }

    @Provides
    @FragmentScope
    @ForActivity
    Context provideActivityContext() {
        return provideActivity();
    }

}
