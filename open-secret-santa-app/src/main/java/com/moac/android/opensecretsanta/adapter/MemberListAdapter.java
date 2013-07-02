package com.moac.android.opensecretsanta.adapter;

import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.PersistableObject;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MemberListAdapter extends ArrayAdapter<MemberRowDetails> {

    private final String TAG = MemberListAdapter.class.getSimpleName();

    private int mResource;

    public MemberListAdapter(Context _context, int _resource, List<MemberRowDetails> _items) {
        super(_context, _resource, _items);
        mResource = _resource;
    }

    @Override
    public boolean hasStableIds() {
        return true; // Required for using ListView#getCheckItemIds
    }

    @Override
    public long getItemId(int position) {
        // Return a stable id - the member id
        return getItem(position).getMember().getId();
    }

    @Override
    public View getView(int _position, View _convertView, ViewGroup _parent) {

        Log.v(TAG, "getView() - creating for position: " + _position);

        View v = _convertView;

        ImageView avatarView;
        TextView memberNameView;
        TextView contactAddressView;
        TextView restrictionsView;
        TextView sendStatusTextView;

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
            sendStatusTextView = (TextView) v.findViewById(R.id.sent_status_textview);

            v.setTag(R.id.member_imageview, avatarView);
            v.setTag(R.id.member_name_textview, memberNameView);
            v.setTag(R.id.contact_address_textview, contactAddressView);
            v.setTag(R.id.restriction_count_textview, restrictionsView);
            v.setTag(R.id.sent_status_textview, sendStatusTextView);
        } else {
            // Recycled View is available, retrieve the holder instance from the View
            avatarView = (ImageView) v.getTag(R.id.member_imageview);
            memberNameView = (TextView) v.getTag(R.id.member_name_textview);
            contactAddressView = (TextView) v.getTag(R.id.contact_address_textview);
            restrictionsView = (TextView) v.getTag(R.id.restriction_count_textview);
            sendStatusTextView = (TextView) v.getTag(R.id.sent_status_textview);
        }

        MemberRowDetails row = getItem(_position);
        Member member = row.getMember();
        Assignment assignment = row.getAssignment();

        // Assign the view with its content.
        if(member.getContactId() == PersistableObject.UNSET_ID || member.getLookupKey() == null) {
            Picasso.with(getContext()).load(R.drawable.ic_contact_picture).into(avatarView);
        } else {
            Uri lookupUri = ContactsContract.Contacts.getLookupUri(member.getContactId(), member.getLookupKey());
            Uri contactUri = ContactsContract.Contacts.lookupContact(getContext().getContentResolver(), lookupUri);
            Picasso.with(getContext()).load(contactUri)
              .placeholder(R.drawable.ic_contact_picture).error(R.drawable.ic_contact_picture)
              .into(avatarView);
        }

        memberNameView.setText(member.getName());
        contactAddressView.setText(member.getContactAddress());

        final long restrictionCount = member.getRestrictionCount();
        if(restrictionCount > 0) {
            restrictionsView.setText(String.valueOf(restrictionCount));
            restrictionsView.setVisibility(View.VISIBLE);
        } else {
            restrictionsView.setVisibility(View.GONE);
        }

        sendStatusTextView.setVisibility(assignment == null ? View.INVISIBLE : View.VISIBLE);

        if(assignment != null) {
            setSendStatusText(sendStatusTextView, assignment);
            setStatusColor(sendStatusTextView, assignment);
        }

        return v;
    }

    private void setSendStatusText(TextView _sendStatusTextView, Assignment _assignment) {
        _sendStatusTextView.setText(_assignment.getSendStatus().getText());
    }

    private void setStatusColor(View _view, Assignment _assignment) {
        switch(_assignment.getSendStatus()) {
            case Sent:
            case Revealed:
                _view.setBackgroundResource(android.R.color.holo_green_light);
                break;
            case Failed:
                _view.setBackgroundResource(android.R.color.holo_red_light);
                break;
            case Assigned:
            default:
                _view.setBackgroundResource(android.R.color.holo_orange_light);
        }
    }
}
