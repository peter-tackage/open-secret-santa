package com.moac.android.opensecretsanta.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.activity.ContactModes;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.PersistableObject;
import com.moac.android.opensecretsanta.util.Utils;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.provider.ContactsContract.Contacts;
import static com.moac.android.opensecretsanta.adapter.Queries.Query;

public class SuggestionsAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = SuggestionsAdapter.class.getSimpleName();
    private static final long QUERY_RESULTS_LIMIT = 10;

    List<Member> mItems;
    final Context mContext;

    public SuggestionsAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Member getItem(int index) {
        return mItems.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ImageView avatarView;
        TextView nameView;
        TextView addressView;

        if(v == null) {
            v = LayoutInflater.from(mContext).inflate(R.layout.suggestion_row, parent, false);
            avatarView = (ImageView) v.findViewById(R.id.avatar_imageview);
            nameView = (TextView) v.findViewById(R.id.name_textview);
            addressView = (TextView)v.findViewById(R.id.address_textview);

            v.setTag(R.id.avatar_imageview, avatarView);
            v.setTag(R.id.name_textview, nameView);
            v.setTag(R.id.address_textview, addressView);
        } else {
            avatarView = (ImageView) v.getTag(R.id.avatar_imageview);
            nameView = (TextView) v.getTag(R.id.name_textview);
            addressView = (TextView) v.getTag(R.id.address_textview);
        }
        Member item = mItems.get(position);

            // This is broken - doesn't immediately update ImageView, needs reload.
//            Log.i(TAG, "getView() - Getting picasso to load: " + item.mAvatarUrl);
//        Picasso.with(mContext).load(contactUri)
//          .placeholder(R.drawable.ic_contact_picture)
//          .into(avatarView);
        // TODO Move these to a background thread/get Picasso to work.
        Drawable avatar = getContentDrawable(mContext, item.getContactId(), item.getLookupKey());
        if (avatar != null) {
            Log.i(TAG, "getView() - setting real avatar" + item.getContactAddress());
            avatarView.setImageDrawable(avatar);
        } else{
            Log.i(TAG, "getView() - setting dummy avatar" + item.getContactAddress());
            avatarView.setImageResource(R.drawable.ic_contact_picture);
        }
        nameView.setText(item.getName());
        addressView.setText(item.getContactAddress());

        return v;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null) {
                    // Retrieve the autoComplete results.
                    mItems = autoComplete(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = mItems;
                    filterResults.count = mItems.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    private List<Member> autoComplete(String _constraint) {
        List<Member> results = new ArrayList<Member>();

        {
            Member manualSuggestion = new Member();
            manualSuggestion.setName(_constraint);
            manualSuggestion.setContactMode(ContactModes.NAME_ONLY_CONTACT_MODE);
            results.add(manualSuggestion);
        }

//        // Note: By using the CONTENT_FILTER_URI we get a reduced set of contact
//        // only those that have been synced. If you want everything - use CONTENT_URI.
//        Uri filterUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI, queryString);
//        Cursor cursor = mContext.getContentResolver().query(
//          filterUri,
//          Queries.ContactQuery.PROJECTION,
//          null,
//          null,
//          null);

        Cursor phoneCursor = null;
        Cursor emailCursor = null;

        try {
            phoneCursor = doQuery(mContext, Queries.PHONE, _constraint);
            results.addAll(processResults(phoneCursor, ContactModes.SMS_CONTACT_MODE));

            emailCursor = doQuery(mContext, Queries.EMAIL, _constraint);
            results.addAll(processResults(emailCursor, ContactModes.EMAIL_CONTACT_MODE));
        } finally {
            if(phoneCursor != null)
                phoneCursor.close();
            if(emailCursor != null)
                emailCursor.close();
        }

        return results;
    }

    public static InputStream openPhoto(Context context, long _contactId, String lookupKey) {

        // Best performance lookup for Contact.
        Uri lookupUri = Contacts.getLookupUri(_contactId, lookupKey);
        Uri contactUri = ContactsContract.Contacts.lookupContact(context.getContentResolver(), lookupUri);

        // Build the URI for the Contact's photo.
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

        Log.i(TAG, "openPhoto() #### Querying to: " + photoUri);
        Cursor cursor = context.getContentResolver().query(photoUri,
          new String[]{ ContactsContract.Contacts.Photo.PHOTO }, null, null, null);
        if(cursor == null) {
            return null;
        }
        try {
            if(cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if(data != null) {
                    return new ByteArrayInputStream(data);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    private static Drawable getContentDrawable(Context _context, long _contactId, String _lookupKey) {
        try {

            if (_contactId == PersistableObject.UNSET_ID || _lookupKey == null)
                return null;

            InputStream stream = openPhoto(_context, _contactId, _lookupKey);
            if(stream == null) {
                throw new FileNotFoundException("Failed to open drawable for lookupKey " + _lookupKey);
            }
            try {
                return Drawable.createFromStream(stream, null);
            } finally {
                Utils.safeClose(stream);
            }
        } catch(FileNotFoundException fnfe) {
            Log.w(TAG, "Icon not found for lookupKey: " + _lookupKey + ", " + fnfe.getMessage());
            return null;
        }
    }

    public static Cursor doQuery(Context _context, Query _query, String _constraint) {
        final Uri.Builder builder = _query.getContentFilterUri().buildUpon()
          .appendPath(_constraint)
          .appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY,
            String.valueOf(QUERY_RESULTS_LIMIT));
        final Cursor cursor = _context.getContentResolver().query(
          builder.build(), _query.getProjection(), _query.getSelection(), null, null);
        return cursor;
    }

    public static List<Member> processResults(Cursor _cursor, int _contactMode) {
        List<Member> results = new ArrayList<Member>();
        // Iterate through the cursor and build up a suggestions array.
        Log.i(TAG, "autoComplete() - cursor length: " + _cursor.getCount());
        Log.i(TAG, "autoComplete() - cursor cols: " + Arrays.toString(_cursor.getColumnNames()));
        while(_cursor.moveToNext()) {

            long id = _cursor.getLong(Query.CONTACT_ID);
            String name = _cursor.getString(Query.NAME);
            String address = _cursor.getString(Query.DESTINATION);
            String lookupKey = _cursor.getString(Query.LOOKUP_KEY);

            Member member = new Member();
            member.setName(name);
            member.setContactId(id);
            member.setLookupKey(lookupKey);
            member.setContactAddress(address);
            member.setContactMode(_contactMode);
            results.add(member);
        }
        return results;
    }
}

