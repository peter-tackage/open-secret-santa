package com.moac.android.opensecretsanta.instrumentation;

import android.content.Context;

import com.facebook.stetho.Stetho;

public class DebugInstrumentation implements Instrumentation {

    private final Context mContext;

    public DebugInstrumentation(Context context) {
        mContext = context;
    }

    @Override
    public void init() {
        Stetho.initialize(
                Stetho.newInitializerBuilder(mContext)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(mContext))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(mContext))
                        .build());
    }
}
