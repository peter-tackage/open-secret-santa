package com.moac.android.opensecretsanta.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.util.ContactUtils;

import java.util.List;

public class MemberListAdapter extends ArrayAdapter<Member> {

    private final String TAG = MemberListAdapter.class.getSimpleName();

    private int mResource;

    public MemberListAdapter(Context _context, int _resource,
                             List<Member> _members) {
        super(_context, _resource, _members);
        mResource = _resource;
    }

    @Override
    public View getView(int _position, View _convertView, ViewGroup _parent) {

        Log.v(TAG, "getView() - creating for position: " + _position);

        View v = _convertView;
        ImageView avatarView;
        TextView memberNameView;
        TextView contactAddressView;
        TextView restrictionsView;

        // Attempt to reuse recycled view if possible
        // Refer - http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
        // More up-to-date info here - http://www.piwai.info/android-adapter-good-practices/ (specifically using Tag)
        // Good info on LayoutInflater here - http://stackoverflow.com/questions/5026926/making-sense-of-layoutinflater

        if(v == null) {
            v = LayoutInflater.from(getContext()).inflate(mResource, _parent, false);

            avatarView = (ImageView) v.findViewById(R.id.member_imageview);
            memberNameView = (TextView) v.findViewById(R.id.member_name_textview);
            contactAddressView = (TextView) v.findViewById(R.id.contact_address_textview);
            restrictionsView = (TextView) v.findViewById(R.id.restriction_count_textview);

            v.setTag(R.id.member_imageview, avatarView);
            v.setTag(R.id.member_name_textview, memberNameView);
            v.setTag(R.id.contact_address_textview, contactAddressView);
            v.setTag(R.id.restriction_count_textview, restrictionsView);
        } else {
            // Recycled View is available, retrieve the holder instance from the View
            avatarView = (ImageView) v.getTag(R.id.member_imageview);
            memberNameView = (TextView)v.getTag(R.id.member_name_textview);
            contactAddressView = (TextView)v.getTag(R.id.contact_address_textview);
            restrictionsView = (TextView)v.getTag(R.id.restriction_count_textview);
        }

        Member item = getItem(_position);

        // Assign the view with its content.
        Drawable avatar = ContactUtils.getContactPhoto(getContext(), item.getContactId(), item.getLookupKey());
        if (avatar != null) {
            avatarView.setImageDrawable(avatar);
        } else{
            avatarView.setImageResource(R.drawable.ic_contact_picture);
        }
        memberNameView.setText(item.getName());
        contactAddressView.setText(item.getContactAddress());

        final long restrictionCount = item.getRestrictionCount();
        if(restrictionCount > 0) {
            restrictionsView.setText(String.valueOf(restrictionCount));
            restrictionsView.setVisibility(View.VISIBLE);
        } else {
            restrictionsView.setVisibility(View.GONE);
        }

        return v;
    }
}
