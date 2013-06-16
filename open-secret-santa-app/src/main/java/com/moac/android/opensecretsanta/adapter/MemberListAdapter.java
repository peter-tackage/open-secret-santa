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
        TextView memberNameView;
        TextView contactModeView;
        TextView restrictionsView;

        // Attempt to reuse recycled view if possible
        // Refer - http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
        // More up-to-date info here - http://www.piwai.info/android-adapter-good-practices/ (specifically using Tag)
        // Good info on LayoutInflater here - http://stackoverflow.com/questions/5026926/making-sense-of-layoutinflater

        if(v == null) {
            LinearLayout root = new LinearLayout(getContext());
            v = LayoutInflater.from(getContext()).inflate(mResource, root, false);

            memberNameView = (TextView) v.findViewById(R.id.member_name_textview);
            contactModeView = (TextView) v.findViewById(R.id.contact_mode_textview);
            restrictionsView = (TextView) v.findViewById(R.id.restriction_count_textview);

            v.setTag(R.id.member_name_textview, memberNameView);
            v.setTag(R.id.contact_mode_textview, contactModeView);
            v.setTag(R.id.restriction_count_textview, restrictionsView);
        } else {
            // Recycled View is available, retrieve the holder instance from the View
            memberNameView = (TextView)v.getTag(R.id.member_name_textview);
            contactModeView = (TextView)v.getTag(R.id.contact_mode_textview);
            restrictionsView = (TextView)v.getTag(R.id.restriction_count_textview);
        }

        MemberRowDetails details = getItem(_position);

        // Assign the view with its content.
        memberNameView.setText(details.mMemberName);
        contactModeView.setText(details.mContactDetail);

        if(details.mRestrictionCount > 0) {
            restrictionsView.setText(String.valueOf(details.mRestrictionCount));
            restrictionsView.setVisibility(View.VISIBLE);
        } else {
            restrictionsView.setVisibility(View.GONE);
        }

        return v;
    }
}
