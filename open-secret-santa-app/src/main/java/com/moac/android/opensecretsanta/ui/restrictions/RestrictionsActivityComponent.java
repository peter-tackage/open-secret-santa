package com.moac.android.opensecretsanta.ui.restrictions;

import com.moac.android.opensecretsanta.OpenSecretSantaApplicationComponent;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.inject.base.component.ActivityScope;
import com.moac.android.opensecretsanta.inject.base.component.BaseActivityComponent;
import com.moac.android.opensecretsanta.inject.base.module.BaseActivityModule;

import android.support.v4.app.Fragment;

import dagger.Component;

@ActivityScope
@Component(dependencies = OpenSecretSantaApplicationComponent.class,
        modules = BaseActivityModule.class)
interface RestrictionsActivityComponent extends BaseActivityComponent {

    DatabaseManager getDatabaseManager();

    void inject(RestrictionsActivity restrictionsActivity);

}
