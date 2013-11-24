package com.moac.android.opensecretsanta.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;

public class DrawerSectionHeaderItem implements DrawerListAdapter.Item {

    private final String mLabel;

    public DrawerSectionHeaderItem(String label) {
        mLabel = label;
    }

    @Override
    public int getItemType() {
        return DrawerListAdapter.ItemType.SECTION_HEADER.ordinal();
    }

    @Override
    public long getItemId() {
        return -1;
    }

    @Override
    public View getView(Context _context, View _convertView, ViewGroup _parent) {
        View view = _convertView;
        TextView labelView;
        if(view == null) {
            view = LayoutInflater.from(_context).inflate(R.layout.drawer_section_header_view, _parent, false);
            labelView = (TextView) view.findViewById(R.id.tv_section_header_label);
            view.setTag(R.id.tv_section_header_label, labelView);
        } else {
            labelView = (TextView) view.getTag(R.id.tv_section_header_label);
        }
        labelView.setText(mLabel);

        return view;
    }
}
