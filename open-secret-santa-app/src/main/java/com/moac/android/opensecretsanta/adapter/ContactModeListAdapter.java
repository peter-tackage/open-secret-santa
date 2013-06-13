package com.moac.android.opensecretsanta.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.activity.ContactModes;

import java.util.List;

public class ContactModeListAdapter extends ArrayAdapter<ContactModeRowDetails> {

    private final static String TAG = ContactModeListAdapter.class.getSimpleName();

    private int mResource;

    public ContactModeListAdapter(Context _context, int _resource,
                                  List<ContactModeRowDetails> _items) {
        super(_context, _resource, _items);
        mResource = _resource;
    }

    @Override
    public View getView(int _position, View _convertView, ViewGroup _parent) {

        View v = _convertView;
        ViewHolder holder;
        if(v == null) {
            LinearLayout root = new LinearLayout(getContext());
            LayoutInflater inflator = LayoutInflater.from(getContext());
            v = inflator.inflate(mResource, root, true);

            holder = new ViewHolder();
            holder.mContactModeImageView = (ImageView) v.findViewById(R.id.contactModeImage);
            holder.mContactModeView = (TextView) v.findViewById(R.id.contactModeTextView);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        ContactModeRowDetails modeDetails = getItem(_position);

        holder.mContactModeView.setText(modeDetails.toString());

        if(modeDetails.mContactMode == ContactModes.SMS_CONTACT_MODE) {
            holder.mContactModeImageView.setImageResource(R.drawable.ic_phone);
        } else if(modeDetails.mContactMode == ContactModes.NAME_ONLY_CONTACT_MODE) {
            holder.mContactModeImageView.setImageResource(R.drawable.ic_menu_view);
        } else if(modeDetails.mContactMode == ContactModes.EMAIL_CONTACT_MODE) {
            holder.mContactModeImageView.setImageResource(R.drawable.ic_email);
        }

        return v;
    }

    private class ViewHolder {
        ImageView mContactModeImageView;
        TextView mContactModeView;
    }
}