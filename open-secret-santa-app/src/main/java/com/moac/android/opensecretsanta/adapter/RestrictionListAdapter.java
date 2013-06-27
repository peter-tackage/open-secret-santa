package com.moac.android.opensecretsanta.adapter;

import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.model.PersistableObject;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

public class RestrictionListAdapter extends BaseAdapter {

    private final static String TAG = RestrictionListAdapter.class.getSimpleName();

    private final Context mContext;
    private OnClickListener mRestrictClickListener;
    private List<RestrictionRowDetails> mItems = Collections.emptyList();

    public RestrictionListAdapter(Context _context, List<RestrictionRowDetails> _items, OnClickListener _onClickListener) {
        mContext = _context;
        mItems = _items;
        mRestrictClickListener = _onClickListener;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public RestrictionRowDetails getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position; // Not meaningful.
    }

    @Override
    public View getView(int _position, View _convertView, ViewGroup _parent) {

        View v = _convertView;
        ImageView memberImageView;
        TextView memberNameView;
        CheckBox checkBoxView;

        if(v == null) {
            v = LayoutInflater.from(mContext).inflate(R.layout.restriction_row, _parent, false);

            memberImageView = (ImageView) v.findViewById(R.id.member_imageview);
            memberNameView = (TextView) v.findViewById(R.id.member_name_textview);
            checkBoxView = (CheckBox) v.findViewById(R.id.restrict_checkbox);

            v.setTag(R.id.member_imageview, memberImageView);
            v.setTag(R.id.member_name_textview, memberNameView);
            v.setTag(R.id.restrict_checkbox, checkBoxView);

        } else {
            memberImageView = (ImageView)v.getTag(R.id.member_imageview);
            memberNameView = (TextView)v.getTag(R.id.member_name_textview);
            checkBoxView = (CheckBox)v.getTag(R.id.restrict_checkbox);
        }

        RestrictionRowDetails item = getItem(_position);

        // Assign the view with its content.
        if(item.getContactId() == PersistableObject.UNSET_ID || item.getLookupKey() == null) {
            Picasso.with(mContext).load(R.drawable.ic_contact_picture).into(memberImageView);
        } else {
            Uri lookupUri = ContactsContract.Contacts.getLookupUri(item.getContactId(), item.getLookupKey());
            Uri contactUri = ContactsContract.Contacts.lookupContact(mContext.getContentResolver(), lookupUri);
            Picasso.with(mContext).load(contactUri)
              .placeholder(R.drawable.ic_contact_picture).error(R.drawable.ic_contact_picture)
              .into(memberImageView);
        }

        memberNameView.setText(item.getToMemberName());
        checkBoxView.setChecked(!item.isRestricted());
        checkBoxView.setOnClickListener(mRestrictClickListener);
        checkBoxView.setTag(item); // Provide item as tag data for callbacks

        return v;
    }

}