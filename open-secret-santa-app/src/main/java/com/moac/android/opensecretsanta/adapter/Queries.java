package com.moac.android.opensecretsanta.adapter;

import android.net.Uri;
import android.provider.ContactsContract;
import android.content.res.Resources;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;

public class Queries {

    public static final Query PHONE = new Query(new String[]{
      Contacts.DISPLAY_NAME_PRIMARY,       // 0
      Phone.NUMBER,                // 1
      Phone.CONTACT_ID,            // 2
      Contacts.PHOTO_THUMBNAIL_URI,// 4
      Phone.LOOKUP_KEY             // 5
    }, Phone.CONTENT_FILTER_URI, Phone.CONTENT_URI) {

        @Override
        public CharSequence getTypeLabel(Resources res, int type, CharSequence label) {
            return Phone.getTypeLabel(res, type, label);
        }

        private final String SELECTION =
          Phone.TYPE + "=" + Phone.TYPE_MOBILE
            + " or " + Phone.TYPE + "=" + Phone.TYPE_MAIN
            + " or " + Phone.TYPE + "=" + Phone.TYPE_HOME
            + " or " + Phone.TYPE + "=" + Phone.TYPE_WORK_MOBILE
            + " or " + Phone.TYPE + "=" + Phone.TYPE_WORK;

        public String getSelection() {
            return SELECTION;
        }
    };

    public static final Query EMAIL = new Query(new String[]{
      Contacts.DISPLAY_NAME_PRIMARY,       // 0
      Email.DATA,                  // 1
      Email.CONTACT_ID,            // 2
      Contacts.PHOTO_THUMBNAIL_URI,// 4
      Email.LOOKUP_KEY             // 5
    }, Email.CONTENT_FILTER_URI, Email.CONTENT_URI) {

        @Override
        public CharSequence getTypeLabel(Resources res, int type, CharSequence label) {
            return Email.getTypeLabel(res, type, label);
        }
    };

    static abstract class Query {
        private final String[] mProjection;
        private final Uri mContentFilterUri;
        private final Uri mContentUri;

        public static final int NAME = 0;                // String
        public static final int DESTINATION = 1;         // String
        public static final int CONTACT_ID = 2;          // long
        public static final int PHOTO_THUMBNAIL_URI = 3; // String
        public static final int LOOKUP_KEY = 4; // String

        public Query(String[] projection, Uri contentFilter, Uri content) {
            mProjection = projection;
            mContentFilterUri = contentFilter;
            mContentUri = content;
        }

        public String[] getProjection() {
            return mProjection;
        }

        public Uri getContentFilterUri() {
            return mContentFilterUri;
        }

        public Uri getContentUri() {
            return mContentUri;
        }

        public String getSelection() {
            return null;
        }

        public String[] getSelectionArgs() {
            return null;
        }

        public abstract CharSequence getTypeLabel(Resources res, int type, CharSequence label);
    }
}
