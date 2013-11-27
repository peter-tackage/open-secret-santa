package com.moac.android.opensecretsanta.adapter;

import android.accounts.Account;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * All because the default toString() of Account objects isn't appropriate
 * when showing a list of email addresses and I don't want another wrapper
 * object and the usual buildList boilerplate.
 */
public class AccountAdapter extends ArrayAdapter<Account> {

    public AccountAdapter(Context context, Account[] accounts) {
        super(context, android.R.layout.simple_spinner_item, accounts);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv = (TextView) super.getView(position, convertView, parent);
        tv.setText(getItem(position).name);
        return tv;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
        tv.setText(getItem(position).name);
        return tv;
    }
}
