package com.moac.android.opensecretsanta.ui.edit;

import com.moac.android.opensecretsanta.OpenSecretSantaApplicationComponent;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.inject.base.component.ActivityScope;
import com.moac.android.opensecretsanta.inject.base.component.BaseActivityComponent;
import com.moac.android.opensecretsanta.inject.base.module.BaseActivityModule;

import dagger.Component;

@ActivityScope
@Component(dependencies = OpenSecretSantaApplicationComponent.class,
        modules = BaseActivityModule.class)
public interface EditActivityComponent extends BaseActivityComponent {

    DatabaseManager getDatabaseManager();

    void inject(EditActivity editActivity);

}
