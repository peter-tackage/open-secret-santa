package com.moac.android.opensecretsanta.content;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.*;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class ContactSuggestionsContentProvider extends ContentProvider {

    public static String TAG = "SuggestionsProvider";

    public static final String AUTHORITY = "com.moac.android.opensecretsanta.provider.contactsuggestions";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/search");
    public static final int MANUAL_ENTRY_ID = -1;

    // UriMatcher constant for search suggestions
    private static final int SEARCH_SUGGEST = 1;

    private static final UriMatcher uriMatcher;

    private static final String[] SEARCH_SUGGESTIONS_COLUMNS = {
      "_id",
      SearchManager.SUGGEST_COLUMN_TEXT_1,
      SearchManager.SUGGEST_COLUMN_TEXT_2,
      SearchManager.SUGGEST_COLUMN_ICON_1,
      SearchManager.SUGGEST_COLUMN_ICON_2
    };

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
    }

    private static final String KEY_SEARCH_COLUMN = ContactsContract.Contacts.DISPLAY_NAME;

    private SearchableInfo mContactsSearchableInfo;

    @Override
    public boolean onCreate() {
        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        if(searchManager != null) {
            List<SearchableInfo> searchables = searchManager.getSearchablesInGlobalSearch();
            // Try to use the "contacts" global search provider
            for(SearchableInfo inf : searchables) {
                if(inf.getSuggestAuthority() != null && inf.getSuggestAuthority().equals("com.android.contacts")) {
                    mContactsSearchableInfo = inf;
                    break;
                }
            }
        }
        return mContactsSearchableInfo != null;
    }

    @Override
    public String getType(Uri uri) {
        switch(uriMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        Log.i(TAG, "query() - uri:" + uri);

        // Use the UriMatcher to see what kind of query we have
        switch(uriMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                Log.i(TAG, "query() - matched SEARCH_SUGGEST");

                // The search term (perhaps partial) of interest
                // See com.android.globalsearch.SuggestionProvider
                String query;
                if (uri.getPathSegments().size() > 1) {
                    query = uri.getLastPathSegment().toLowerCase();
                } else {
                    query = "";
                }

                // Get a suggestions cursor from the Contacts Content Provider
                // These calls effectively replicate the calls made by the SearchManager, without the baggage of the framework.
                Uri suggestionsBaseUri = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, SearchManager.SUGGEST_URI_PATH_QUERY);
                Uri suggestionsQueryUri = Uri.withAppendedPath(suggestionsBaseUri, query);
                Cursor contactsCursor = getContext().getContentResolver().query(suggestionsQueryUri, null, null, null, null);
                //Cursor contactsCursor = getSuggestions(mContactsSearchableInfo, query, limit);
                Log.i(TAG, "query() - cursor length:" + contactsCursor.getCount());
                Log.i(TAG, "query() - cursor column names:" + Arrays.toString(contactsCursor.getColumnNames()));

                // We need to add our own custom entries to the suggestions list.
                MatrixCursor manualCursor = new MatrixCursor(SEARCH_SUGGESTIONS_COLUMNS);
                if(!query.isEmpty())
                    manualCursor.addRow(new Object[]{ MANUAL_ENTRY_ID, query, "Add name only entry", null, null });

                MergeCursor merged = new MergeCursor(new Cursor[]{ manualCursor, contactsCursor});

                return merged;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String arg1, String[] arg2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues arg1, String arg2, String[] arg3) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri insert(Uri uri, ContentValues arg1) {
        throw new UnsupportedOperationException();
    }

//    /**
//     * This is essentially a copy of the method in SearchManager - it is not publically available.
//     *
//     * @param searchable
//     * @param query
//     * @param limit
//     * @return
//     */
//    public Cursor getSuggestions(SearchableInfo searchable, String query, int limit) {
//        if(searchable == null) {
//            return null;
//        }
//
//        String authority = searchable.getSuggestAuthority();
//        if(authority == null) {
//            return null;
//        }
//
//        Uri.Builder uriBuilder = new Uri.Builder()
//          .scheme(ContentResolver.SCHEME_CONTENT)
//          .authority(authority)
//          .query("")  // TODO: Remove, workaround for a bug in Uri.writeToParcel()
//          .fragment("");  // TODO: Remove, workaround for a bug in Uri.writeToParcel()
//
//        // if content path provided, insert it now
//        final String contentPath = searchable.getSuggestPath();
//        if(contentPath != null) {
//            uriBuilder.appendEncodedPath(contentPath);
//        }
//
//        // append standard suggestion query path
//        uriBuilder.appendPath(SearchManager.SUGGEST_URI_PATH_QUERY);
//
//        // get the query selection, may be null
//        String selection = searchable.getSuggestSelection();
//        // inject query, either as selection args or inline
//        String[] selArgs = null;
//        if(selection != null) {    // use selection if provided
//            selArgs = new String[]{ query };
//        } else {                    // no selection, use REST pattern
//            uriBuilder.appendPath(query);
//        }
//
//        if(limit > 0) {
//            uriBuilder.appendQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT, String.valueOf(limit));
//        }
//
//        Uri uri = uriBuilder.build();
//
//        // finally, make the query
//        return getContext().getContentResolver().query(uri, null, selection, selArgs, null);
//    }
}

