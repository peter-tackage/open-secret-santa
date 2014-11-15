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

public class MemberListAdapter extends ArrayAdapter<MemberRowDetails> {

    private final String TAG = MemberListAdapter.class.getSimpleName();

    private int mResource;

    public MemberListAdapter(Context context, int resource) {
        super(context, resource);
        mResource = resource;
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
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.v(TAG, "getView() - creating for position: " + position);

        View v = convertView;

        ImageView avatarView;
        TextView memberNameView;
        TextView contactAddressView;
        TextView restrictionsView;
        TextView sendStatusTextView;

        // Attempt to reuse recycled view if possible
        // Refer - http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
        // More up-to-date info here - http://www.piwai.info/android-adapter-good-practices/ (specifically using Tag)
        // Good info on LayoutInflater here - http://stackoverflow.com/questions/5026926/making-sense-of-layoutinflater

        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(mResource, parent, false);

            avatarView = (ImageView) v.findViewById(R.id.imageView_avatar);
            memberNameView = (TextView) v.findViewById(R.id.textView_member_name);
            contactAddressView = (TextView) v.findViewById(R.id.textView_contact_address);
            restrictionsView = (TextView) v.findViewById(R.id.textView_restriction_count);
            sendStatusTextView = (TextView) v.findViewById(R.id.textView_sent_status);

            v.setTag(R.id.imageView_avatar, avatarView);
            v.setTag(R.id.textView_member_name, memberNameView);
            v.setTag(R.id.textView_contact_address, contactAddressView);
            v.setTag(R.id.textView_restriction_count, restrictionsView);
            v.setTag(R.id.textView_sent_status, sendStatusTextView);
        } else {
            // Recycled View is available, retrieve the holder instance from the View
            avatarView = (ImageView) v.getTag(R.id.imageView_avatar);
            memberNameView = (TextView) v.getTag(R.id.textView_member_name);
            contactAddressView = (TextView) v.getTag(R.id.textView_contact_address);
            restrictionsView = (TextView) v.getTag(R.id.textView_restriction_count);
            sendStatusTextView = (TextView) v.getTag(R.id.textView_sent_status);
        }

        MemberRowDetails row = getItem(position);
        Member member = row.getMember();
        Assignment assignment = row.getAssignment();

        // Assign the view with its content.
        if (member.getContactId() == PersistableObject.UNSET_ID || member.getLookupKey() == null) {
            Picasso.with(getContext()).load(R.drawable.ic_contact_picture)
                    .transform(new RoundEdgeTransformation())
                    .into(avatarView);
        } else {
            Uri lookupUri = ContactsContract.Contacts.getLookupUri(member.getContactId(), member.getLookupKey());
            Uri contactUri = ContactsContract.Contacts.lookupContact(getContext().getContentResolver(), lookupUri);
            Picasso.with(getContext()).load(contactUri)
                    .placeholder(R.drawable.ic_contact_picture).error(R.drawable.ic_contact_picture)
                    .transform(new RoundEdgeTransformation())
                    .into(avatarView);
        }

        memberNameView.setText(member.getName());
        contactAddressView.setText(member.getContactDetails());

        final long restrictionCount = member.getRestrictionCount();
        if (restrictionCount > 0) {
            restrictionsView.setText(String.valueOf(restrictionCount) + " " + getContext().getString(R.string.restrictions_label) + (restrictionCount > 1 ? "s" : ""));
            restrictionsView.setVisibility(View.VISIBLE);
        } else {
            restrictionsView.setVisibility(View.GONE);
        }

        sendStatusTextView.setVisibility(assignment == null ? View.INVISIBLE : View.VISIBLE);

        if (assignment != null) {
            setSendStatusText(sendStatusTextView, assignment);
            setStatusColor(sendStatusTextView, assignment);
        }

        return v;
    }

    private void setSendStatusText(TextView sendStatusTextView, Assignment assignment) {
        sendStatusTextView.setText(assignment.getSendStatus().getText());
    }

    private void setStatusColor(View view, Assignment assignment) {
        switch (assignment.getSendStatus()) {
            case Sent:
            case Revealed:
                view.setBackgroundResource(R.color.revealed);
                break;
            case Failed:
                view.setBackgroundResource(R.color.failed);
                break;
            case Assigned:
            default:
                view.setBackgroundResource(R.color.assigned);
        }
    }
}
