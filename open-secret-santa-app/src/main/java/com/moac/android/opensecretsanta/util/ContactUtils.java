package com.moac.android.opensecretsanta.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import com.moac.android.opensecretsanta.model.PersistableObject;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ContactUtils {

    private static final String TAG = ContactUtils.class.getSimpleName();

//    public static InputStream openPhoto(Context context, long _contactId, String lookupKey) {
//
//        // Dual key - best performance lookup for Contact.
//        Uri lookupUri = ContactsContract.Contacts.getLookupUri(_contactId, lookupKey);
//        Uri contactUri = ContactsContract.Contacts.lookupContact(context.getContentResolver(), lookupUri);
//
//        // Build the URI for the Contact's photo.
//        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
//
//        Log.i(TAG, "openPhoto() #### Querying to: " + photoUri);
//        Cursor cursor = context.getContentResolver().query(photoUri,
//          new String[]{ ContactsContract.Contacts.Photo.PHOTO }, null, null, null);
//
//        if(cursor == null)
//            return null;
//
//        try {
//            if(cursor.moveToFirst()) {
//                byte[] data = cursor.getBlob(0);
//                if(data != null) {
//                    return new ByteArrayInputStream(data);
//                }
//            }
//        } finally {
//            cursor.close();
//        }
//        return null;
//    }

    public static Drawable getContactPhoto(Context _context, long _contactId, String _lookupKey) {
        try {

            if(_contactId == PersistableObject.UNSET_ID || _lookupKey == null)
                return null;

            // Dual key - best performance lookup for Contact.
            Uri lookupUri = ContactsContract.Contacts.getLookupUri(_contactId, _lookupKey);
            Uri contactUri = ContactsContract.Contacts.lookupContact(_context.getContentResolver(), lookupUri);

            InputStream stream = ContactsContract.Contacts.openContactPhotoInputStream(_context.getContentResolver(),contactUri);
            if(stream == null) {
                throw new FileNotFoundException("Failed to open drawable for lookupKey " + _lookupKey);
            }
            try {
                return Drawable.createFromStream(stream, null);
            } finally {
                Utils.safeClose(stream);
            }
        } catch(FileNotFoundException fnfe) {
            Log.w(TAG, "Photo not found for lookupKey: " + _lookupKey + ", " + fnfe.getMessage());
            return null;
        }
    }
}
