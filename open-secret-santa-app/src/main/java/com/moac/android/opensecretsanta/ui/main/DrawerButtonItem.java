package com.moac.android.opensecretsanta.ui.main;

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
    public View getView(Context context, View convertView, ViewGroup parent) {
        View view = convertView;
        TextView textView;
        if(view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.drawer_button_view, parent, false);
            textView = (TextView) view.findViewById(R.id.textView_button_label);
            view.setTag(R.id.textView_button_label, textView);
        } else {
            textView = (TextView) view.getTag(R.id.textView_button_label);
        }

        textView.setText(mText);
        textView.setCompoundDrawablesWithIntrinsicBounds(mDrawable, null, null, null);
        view.setOnClickListener(mListener);
        return view;
    }
}
