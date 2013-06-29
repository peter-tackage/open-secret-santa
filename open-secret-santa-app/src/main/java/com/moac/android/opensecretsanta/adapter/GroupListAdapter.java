package com.moac.android.opensecretsanta.adapter;

import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.PersistableObject;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

public class GroupListAdapter extends BaseAdapter {

    private final Context mContext;
    private List<GroupRowDetails> mGroupRowDetails = Collections.emptyList();

    public GroupListAdapter(Context _context) {
        mContext = _context;
    }

    @Override
    public int getCount() {
        return mGroupRowDetails.size();
    }

    @Override
    public GroupRowDetails getItem(int _position) {
        return mGroupRowDetails.get(_position);
    }

    @Override
    public long getItemId(int _position) {
        return mGroupRowDetails.get(_position).getId();
    }

    @Override
    public View getView(int _position, View _convertView, ViewGroup _parent) {
        View v = _convertView;
        TextView groupNameView;
        TextView groupDateView;
        LinearLayout groupLinearLayout;

        if(v == null) {
            v = LayoutInflater.from(mContext).inflate(R.layout.group_row_view, _parent, false);
            groupNameView = (TextView) v.findViewById(R.id.group_name_textView);
            groupDateView = (TextView) v.findViewById(R.id.group_date_textView);
            groupLinearLayout = (LinearLayout) v.findViewById(R.id.group_linearLayout);
            v.setTag(R.id.group_name_textView, groupNameView);
            v.setTag(R.id.group_date_textView, groupDateView);
            v.setTag(R.id.group_linearLayout, groupLinearLayout);
        } else {
            groupNameView = (TextView) v.getTag(R.id.group_name_textView);
            groupDateView = (TextView) v.getTag(R.id.group_date_textView);
            groupLinearLayout = (LinearLayout) v.getTag(R.id.group_linearLayout);
        }

        GroupRowDetails groupRowDetails = getItem(_position);
        groupNameView.setText(groupRowDetails.getName());
        groupDateView.setText(groupRowDetails.getCreationDate());

        // keep track of how many images we have added, let's cap it at 5 max
        int numAdded = 0;
        for(Member m : groupRowDetails.getMembers()) {

            // Assign the view with its content.
            if(m.getContactId() != PersistableObject.UNSET_ID && m.getLookupKey() != null) {

                boolean hasImage = v.findViewById((int) m.getContactId()) != null;
                if (!hasImage) {
                    ImageView groupPicView = new ImageView(mContext);
                    groupPicView.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
                    groupPicView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    groupPicView.setId((int) m.getContactId());

                    Uri lookupUri = ContactsContract.Contacts.getLookupUri(m.getContactId(), m.getLookupKey());
                    Uri contactUri = ContactsContract.Contacts.lookupContact(mContext.getContentResolver(), lookupUri);
                    Picasso.with(mContext).load(contactUri)
                    .placeholder(R.drawable.ic_contact_picture).error(R.drawable.ic_contact_picture)
                    .into(groupPicView);

                    groupLinearLayout.addView(groupPicView);
                    if (numAdded == 4) {
                        break;
                    } else {
                        numAdded++;
                    }
                }
            }
        }
        return v;
    }

    public void update(List<GroupRowDetails> _groupRowDetails) {
        mGroupRowDetails = _groupRowDetails;
        notifyDataSetChanged();
    }
}
