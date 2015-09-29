package com.moac.android.opensecretsanta.ui.common;

import com.moac.android.opensecretsanta.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

public abstract class BaseEditorActivity extends AppCompatActivity {

    protected Saveable mSaveableFragment;

    @CallSuper
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_generic_editor);
        createEditorFragment(savedInstance);
        configureActionBar();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    // Note: We no longer save on backpress, we give user explicit choices.

    protected abstract void createEditorFragment(Bundle savedInstance);

    private void configureActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Inflate a custom action bar with editor controls
            LayoutInflater inflater = (LayoutInflater) getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams")
            View editorActionBarView = inflater.inflate(R.layout.editor_custom_action_bar, null);
            View okMenuItem = editorActionBarView.findViewById(R.id.menuItem_ok);
            okMenuItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Leave it to Fragment to report save result
                    if (mSaveableFragment.save()) {
                        finish();
                    }
                }
            });
            View discardMenuItem = editorActionBarView.findViewById(R.id.menuItem_discard);
            discardMenuItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            // Show the custom action bar but hide the home icon and title
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                                        ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                                        |
                                        ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setCustomView(editorActionBarView);
        }
    }
}
