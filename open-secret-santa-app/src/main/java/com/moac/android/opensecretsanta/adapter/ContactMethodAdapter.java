package com.moac.android.opensecretsanta.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.moac.android.opensecretsanta.model.ContactMethod;

public class ContactMethodAdapter extends ArrayAdapter<ContactMethod> {

    public ContactMethodAdapter(Context context, ContactMethod[] methods) {
        super(context, android.R.layout.simple_spinner_item, methods);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv = (TextView)super.getView(position, convertView, parent);
        tv.setText(getItem(position).getDisplayText());
        return tv;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView tv = (TextView)super.getDropDownView(position, convertView, parent);
        tv.setText(getItem(position).getDisplayText());
        return tv;
    }
}