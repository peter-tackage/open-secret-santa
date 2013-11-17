package com.moac.android.opensecretsanta.adapter;

import android.accounts.Account;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AccountAdapter extends BaseAdapter {

    private Context mContext;
    private Account[] mAccounts;

    public AccountAdapter(Context context, Account[] accounts) {
        mContext = context;
        mAccounts = accounts;
    }

    @Override
    public int getCount() {
        return mAccounts != null ? mAccounts.length : 0;
    }

    @Override
    public Object getItem(int position) {
        return mAccounts[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv = (TextView)LayoutInflater.from(mContext).inflate(android.R.layout.simple_spinner_item, parent, false);
        tv.setText(mAccounts[position].name);
        return tv;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView tv = (TextView)LayoutInflater.from(mContext).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        tv.setText(mAccounts[position].name);
        return tv;
    }
}
