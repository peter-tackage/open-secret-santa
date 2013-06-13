package com.moac.android.opensecretsanta.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;

import java.util.List;

public class RestrictionListAdapter extends ArrayAdapter<RestrictionRowDetails> {

    private final static String TAG = RestrictionListAdapter.class.getSimpleName();

    private int mResource;
    private OnClickListener mRestrictClickListener;

    public RestrictionListAdapter(Context _context, int _resource,
                                  List<RestrictionRowDetails> _items, OnClickListener _onClickListener) {
        super(_context, _resource, _items);
        mResource = _resource;
        mRestrictClickListener = _onClickListener;
    }

    @Override
    public View getView(int _position, View _convertView, ViewGroup _parent) {

        View v = _convertView;
        ViewHolder holder;

        if(v == null) {
            LinearLayout root = new LinearLayout(getContext());
            v = LayoutInflater.from(getContext()).inflate(mResource, root, false);

            holder = new ViewHolder();
            holder.mCheckBoxView = (CheckBox) v.findViewById(R.id.restrictCheckBox);
            holder.mMemberNameView = (TextView) v.findViewById(R.id.memberNameCell);

            v.setTag(holder);

        } else {
            // Recycled View is available, retrieve the holder instance from the View
            holder = (ViewHolder) v.getTag();
        }

        RestrictionRowDetails res = getItem(_position);

        holder.mMemberNameView.setText(res.getToMemberName());
        holder.mCheckBoxView.setChecked(!res.isRestricted());
        holder.mCheckBoxView.setOnClickListener(mRestrictClickListener);
        holder.mCheckBoxView.setTag(res); // Provide item as tag data for callbacks

        return v;
    }

    private class ViewHolder {
        TextView mMemberNameView;
        CheckBox mCheckBoxView;
    }
}