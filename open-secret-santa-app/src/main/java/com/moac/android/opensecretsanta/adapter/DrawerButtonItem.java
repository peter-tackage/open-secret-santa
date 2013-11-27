package com.moac.android.opensecretsanta.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;

public class DrawerButtonItem implements DrawerListAdapter.Item {

    private final Drawable mDrawable;
    private final String mText;
    private final View.OnClickListener mListener;

    public DrawerButtonItem(Drawable drawable, String text, View.OnClickListener listener) {
        mDrawable = drawable;
        mText = text;
        mListener = listener;
    }

    @Override
    public int getItemType() {
        return DrawerListAdapter.ItemType.BUTTON.ordinal();
    }

    @Override
    public long getItemId() {
        return -1;
    }

    @Override
    public View getView(Context _context, View _convertView, ViewGroup _parent) {
        View view = _convertView;
        TextView textView;
        if(view == null) {
            view = LayoutInflater.from(_context).inflate(R.layout.drawer_button_view, _parent, false);
            textView = (TextView) view.findViewById(R.id.tv_button_label);
            view.setTag(R.id.tv_button_label, textView);
        } else {
            textView = (TextView) view.getTag(R.id.tv_button_label);
        }

        textView.setText(mText);
        textView.setCompoundDrawablesWithIntrinsicBounds(mDrawable, null, null, null);
        view.setOnClickListener(mListener);
        return view;
    }
}
