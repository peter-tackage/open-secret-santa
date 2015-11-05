package com.moac.android.opensecretsanta.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;

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
        TextView contactModeView;

        if(v == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            v = inflater.inflate(mResource, _parent, false);

            contactModeView = (TextView) v.findViewById(R.id.contactModeTextView);
            v.setTag(R.id.contactModeTextView, contactModeView);

        } else {
            contactModeView = (TextView)v.getTag(R.id.contactModeTextView);
        }

        ContactModeRowDetails modeDetails = getItem(_position);
        contactModeView.setText(modeDetails.toString());

        return v;
    }

}