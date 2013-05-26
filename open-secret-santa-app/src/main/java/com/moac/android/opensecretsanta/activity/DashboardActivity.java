package com.moac.android.opensecretsanta.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import com.moac.android.opensecretsanta.R;

public class DashboardActivity extends Activity {

    private final static String TAG = "DashboardActivity";

    private ImageButton mNewDrawImageButton;
    private ImageButton mHistoryImageButton;
    private ImageButton mOptionsImageButton;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Activity State: onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_view);

        // Obtain handles to UI objects
        mNewDrawImageButton = (ImageButton) findViewById(R.id.newDrawImageButton);
        mHistoryImageButton = (ImageButton) findViewById(R.id.historyImageButton);
        mOptionsImageButton = (ImageButton) findViewById(R.id.optionsImageButton);

        // Register progressHandler for UI elements
        mNewDrawImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "mNewDrawButton clicked");
                Intent myIntent = new Intent(v.getContext(), GroupSelectionActivity.class);
                startActivity(myIntent);
            }
        });
        mHistoryImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "mHistoryImageButton clicked");
                Intent myIntent = new Intent(v.getContext(), DrawHistoryActivity.class);
                startActivity(myIntent);
            }
        });
        mOptionsImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "mOptionsImageButton clicked");
                Intent myIntent = new Intent(v.getContext(), AllPreferencesActivity.class);
                startActivity(myIntent);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // TODO Get a race condition here with the groups activity
        // when cycling back quickly from the draw tab.
        // Essentially the group populating thread is still going
        // when this gets called.
        // I've read in a few places that not closing isn't so bad.
        // and to me, it's better than an exception on exit.
        // Also read that using a Provider can help with the
        // management of open/close - perhaps a future enhancement.
        //	OpenSecretSantaDB.getInstance(getApplicationContext()).close();
    }
}
