package com.moac.android.opensecretsanta.ui.main;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class DrawerListAdapter extends ArrayAdapter<DrawerListAdapter.Item> {

    public enum ItemType {BUTTON, GROUP}

    public static interface Item {
        int getItemType();
        long getItemId();
        View getView(Context _context, View _convertView, ViewGroup _parent);
    }

    public DrawerListAdapter(Context _context) {
        super(_context, 0);
    }

    @Override
    public int getViewTypeCount() {
        return ItemType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getItemType();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getItemId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int _position, View _convertView, ViewGroup _parent) {
        // Note: Even though the list contains mixed content, the use of
        // multiple ViewTypes will ensure that only appropriate convertViews
        // are made available for recycling
        return getItem(_position).getView(getContext(), _convertView, _parent);
    }
}
