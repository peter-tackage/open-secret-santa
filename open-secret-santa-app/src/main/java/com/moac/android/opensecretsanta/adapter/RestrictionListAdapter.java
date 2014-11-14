package com.moac.android.opensecretsanta.adapter;

import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

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

    public RestrictionListAdapter(Context context, List<RestrictionRowDetails> items, OnClickListener onClickListener) {
        mContext = context;
        mItems = items;
        mRestrictClickListener = onClickListener;
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
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        ImageView memberImageView;
        TextView memberNameView;
        CheckBox checkBoxView;

        if (v == null) {
            v = LayoutInflater.from(mContext).inflate(R.layout.list_item_restriction, parent, false);

            memberImageView = (ImageView) v.findViewById(R.id.imageView_avatar);
            memberNameView = (TextView) v.findViewById(R.id.textView_member_name);
            checkBoxView = (CheckBox) v.findViewById(R.id.checkBox_restriction);

            v.setTag(R.id.imageView_avatar, memberImageView);
            v.setTag(R.id.textView_member_name, memberNameView);
            v.setTag(R.id.checkBox_restriction, checkBoxView);

        } else {
            memberImageView = (ImageView) v.getTag(R.id.imageView_avatar);
            memberNameView = (TextView) v.getTag(R.id.textView_member_name);
            checkBoxView = (CheckBox) v.getTag(R.id.checkBox_restriction);
        }

        RestrictionRowDetails item = getItem(position);

        // Assign the view with its content.
        if (item.getContactId() == PersistableObject.UNSET_ID || item.getLookupKey() == null) {
            Picasso.with(mContext).load(R.drawable.ic_contact_picture)
                    .transform(new RoundEdgeTransformation())
                    .into(memberImageView);
        } else {
            Uri lookupUri = ContactsContract.Contacts.getLookupUri(item.getContactId(), item.getLookupKey());
            Uri contactUri = ContactsContract.Contacts.lookupContact(mContext.getContentResolver(), lookupUri);
            Picasso.with(mContext).load(contactUri)
                    .placeholder(R.drawable.ic_contact_picture)
                    .error(R.drawable.ic_contact_picture)
                    .transform(new RoundEdgeTransformation())

                    .into(memberImageView);
        }

        memberNameView.setText(item.getToMemberName());
        checkBoxView.setChecked(!item.isRestricted());
        checkBoxView.setOnClickListener(mRestrictClickListener);
        checkBoxView.setTag(item); // Provide item as tag data for callbacks

        return v;
    }

}