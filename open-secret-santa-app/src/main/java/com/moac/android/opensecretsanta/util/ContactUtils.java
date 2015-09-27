package com.moac.android.opensecretsanta.util;

import com.moac.android.opensecretsanta.model.PersistableObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public final class ContactUtils {

    private static final String TAG = ContactUtils.class.getSimpleName();

    private ContactUtils() {
        throw new AssertionError("No instances allowed.");
    }

    public static Drawable getContactPhoto(Context _context, long _contactId, String _lookupKey) {
        try {

            if (_contactId == PersistableObject.UNSET_ID || _lookupKey == null) {
                return null;
            }

            // Dual key - best performance lookup for Contact.
            Uri lookupUri = ContactsContract.Contacts.getLookupUri(_contactId, _lookupKey);
            Uri contactUri = ContactsContract.Contacts
                    .lookupContact(_context.getContentResolver(), lookupUri);

            InputStream stream = ContactsContract.Contacts
                    .openContactPhotoInputStream(_context.getContentResolver(), contactUri);
            if (stream == null) {
                throw new FileNotFoundException(
                        "Failed to open drawable for lookupKey " + _lookupKey);
            }
            try {
                return Drawable.createFromStream(stream, null);
            } finally {
                closeQuietly(stream);
            }
        } catch (FileNotFoundException fnfe) {
            Log.w(TAG, "Photo not found for lookupKey: " + _lookupKey + ", " + fnfe.getMessage());
            return null;
        }
    }

    // FIXME Replace this with Closer
    private static void closeQuietly(final InputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
