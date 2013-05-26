package com.moac.android.opensecretsanta.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.moac.android.opensecretsanta.R;

public class AllPreferencesActivity extends PreferenceActivity {

    private final static String TAG = "AllPreferencesActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
