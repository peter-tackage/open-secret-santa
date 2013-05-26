package com.moac.android.opensecretsanta.activity;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;

public class DrawTabManagerActivity extends TabActivity {

    private final static String TAG = "DrawTabManagerActivity";

    String groupName;
    Long groupId;
    TabHost mTabHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.draw_builder_tabhost_view);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            groupName = extras.getString(Constants.GROUP_NAME);
            groupId = extras.getLong(Constants.GROUP_ID);
        }

        Intent intent;
        TabHost.TabSpec spec;

        mTabHost = getTabHost();
        Resources res = DrawTabManagerActivity.this.getResources();

        Drawable myMembersImage = res.getDrawable(R.drawable.ic_tab_members);
        intent = new Intent().setClass(this, MembersListActivity.class);
        intent.putExtra(Constants.GROUP_ID, groupId);
        setupTab(new TextView(this), myMembersImage, intent);

        Drawable myDrawImage = res.getDrawable(R.drawable.ic_tab_draw);
        intent = new Intent().setClass(this, AssignmentSharerActivity.class);
        intent.putExtra(Constants.GROUP_ID, groupId);
        setupTab(new TextView(this), myDrawImage, intent);
    }

    private void setupTab(final View view, final Drawable drawable, Intent intent) {
        View tabview = createTabView(mTabHost.getContext(), drawable);
        TabSpec spec = mTabHost.newTabSpec("").setIndicator(tabview).setContent(new TabContentFactory() {
            @Override
            public View createTabContent(String tag) {return view;}
        });
        spec.setContent(intent);
        mTabHost.addTab(spec);
    }

    private static View createTabView(final Context context, final Drawable image) {
        View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.tabsImage);
        imageView.setImageDrawable(image);
        return view;
    }
}
