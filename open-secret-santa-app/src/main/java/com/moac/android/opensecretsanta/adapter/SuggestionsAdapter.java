package com.moac.android.opensecretsanta.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.model.ContactMethod;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.util.ContactUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.moac.android.opensecretsanta.adapter.Queries.Query;

// FIXME See API Demos, AutoComplete4 for a potentially better approach
public class SuggestionsAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = SuggestionsAdapter.class.getSimpleName();
    private static final long QUERY_RESULTS_LIMIT = 10;

    List<Member> mItems;
    final Context mContext;
    private Filter mFilter;
    private final Handler mHandler;

    public SuggestionsAdapter(Context context) {
        mContext = context;
        mHandler = new Handler();
        mFilter = new SuggestionFilter(context);
        mItems = Collections.emptyList();
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
            v = LayoutInflater.from(mContext).inflate(R.layout.list_item_suggestion, parent, false);
            avatarView = (ImageView) v.findViewById(R.id.imageView_avatar);
            nameView = (TextView) v.findViewById(R.id.textView_name);
            addressView = (TextView) v.findViewById(R.id.textView_address);

            v.setTag(R.id.imageView_avatar, avatarView);
            v.setTag(R.id.textView_name, nameView);
            v.setTag(R.id.textView_address, addressView);
        } else {
            avatarView = (ImageView) v.getTag(R.id.imageView_avatar);
            nameView = (TextView) v.getTag(R.id.textView_name);
            addressView = (TextView) v.getTag(R.id.textView_address);
        }

        Member item = mItems.get(position);

        // This is broken - doesn't immediately update ImageView, needs reload.
//            Log.i(TAG, "getView() - Getting picasso to load: " + item.mAvatarUrl);
//        Picasso.with(mContext).load(contactUri)
//          .placeholder(R.drawable.ic_contact_picture)
//          .into(avatarView);
        // TODO Move these to a background thread/get Picasso to work.
        Drawable avatar = ContactUtils.getContactPhoto(mContext, item.getContactId(), item.getLookupKey());
        if(avatar != null) {
            avatarView.setImageDrawable(avatar);
        } else {
            avatarView.setImageResource(R.drawable.ic_contact_picture);
        }
        nameView.setText(item.getName());
        addressView.setText(item.getContactDetails());

        return v;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private static List<Member> autoComplete(Context _context, String _constraint) {
        List<Member> results = new ArrayList<Member>();

        {
            Member manualSuggestion = new Member();
            manualSuggestion.setName(_constraint.trim());
            manualSuggestion.setContactMethod(ContactMethod.REVEAL_ONLY);
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
            phoneCursor = doQuery(_context, Queries.PHONE, _constraint);
            results.addAll(processResults(phoneCursor, ContactMethod.SMS));

            emailCursor = doQuery(_context, Queries.EMAIL, _constraint);
            results.addAll(processResults(emailCursor, ContactMethod.EMAIL));
        } finally {
            if(phoneCursor != null)
                phoneCursor.close();
            if(emailCursor != null)
                emailCursor.close();
        }

        return results;
    }

    private static Cursor doQuery(Context _context, Query _query, String _constraint) {
        final Uri.Builder builder = _query.getContentFilterUri().buildUpon()
          .appendPath(_constraint)
          .appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY,
            String.valueOf(QUERY_RESULTS_LIMIT));
        return _context.getContentResolver().query(
          builder.build(), _query.getProjection(), _query.getSelection(), null, null);
    }

    private static List<Member> processResults(Cursor _cursor, ContactMethod _contactMethod) {
        List<Member> results = new ArrayList<Member>();
        // Iterate through the cursor and build up a suggestions array.
        Log.i(TAG, "autoComplete() - cursor length: " + _cursor.getCount());
        Log.i(TAG, "autoComplete() - cursor cols: " + Arrays.toString(_cursor.getColumnNames()));
        while(_cursor.moveToNext()) {

            long id = _cursor.getLong(Query.CONTACT_ID);
            String name = _cursor.getString(Query.NAME);
            String contactDetails = _cursor.getString(Query.DESTINATION);
            String lookupKey = _cursor.getString(Query.LOOKUP_KEY);

            Member member = new Member();
            member.setName(name.trim());
            member.setContactId(id);
            member.setLookupKey(lookupKey);
            member.setContactDetails(contactDetails);
            member.setContactMethod(_contactMethod);
            results.add(member);
        }
        return results;
    }

    private class SuggestionFilter extends Filter {

        Context mFilterContext;

        public SuggestionFilter(Context context) {
            mFilterContext = context;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if(constraint != null) {
                // Assign the data to the FilterResults
                final List<Member> items = autoComplete(mFilterContext, constraint.toString());
                filterResults.values = items;
                filterResults.count = items.size();
                // Try assigning via this post to main.
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mItems = items;
                        notifyDataSetChanged();
                    }
                });
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
    }
}

