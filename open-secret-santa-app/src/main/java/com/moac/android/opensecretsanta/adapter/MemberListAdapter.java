package com.moac.android.opensecretsanta.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;

import java.util.List;

public class MemberListAdapter extends ArrayAdapter<MemberRowDetails> {

    private final String TAG = MemberListAdapter.class.getSimpleName();

    private int mResource;

    public MemberListAdapter(Context _context, int _resource,
                             List<MemberRowDetails> _members) {
        super(_context, _resource, _members);
        mResource = _resource;
    }

    @Override
    public View getView(int _position, View _convertView, ViewGroup _parent) {

        View v = _convertView;
        ViewHolder holder;

        // Attempt to reuse recycled view if possible
        // Refer - http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
        // Good info on LayoutInflater here - http://stackoverflow.com/questions/5026926/making-sense-of-layoutinflater

        if(v == null) {
            LinearLayout root = new LinearLayout(getContext());
            v = LayoutInflater.from(getContext()).inflate(mResource, root, false);

            holder = new ViewHolder();
            holder.mMemberNameView = (TextView) v.findViewById(R.id.member_name_textview);
            holder.mContactModeView = (TextView) v.findViewById(R.id.contact_mode_textview);
            holder.mRestrictionsView = (TextView) v.findViewById(R.id.restriction_count_textview);

            v.setTag(holder);
        } else {
            // Recycled View is available, retrieve the holder instance from the View
            holder = (ViewHolder) v.getTag();
        }

        MemberRowDetails details = getItem(_position);

        // Assign the view with its content.
        holder.mMemberNameView.setText(details.mMemberName);
        holder.mContactModeView.setText(details.mContactDetail);

        if(details.mRestrictionCount > 0) {
            holder.mRestrictionsView.setText(String.valueOf(details.mRestrictionCount));
            holder.mRestrictionsView.setVisibility(View.VISIBLE);
        } else {
            holder.mRestrictionsView.setVisibility(View.GONE);
        }

        return v;
    }

    // ViewHolder keeps references to avoid making unnecessary calls to findViewById
    private class ViewHolder {
        TextView mMemberNameView;
        TextView mRestrictionsView;
        TextView mContactModeView;
    }
}
