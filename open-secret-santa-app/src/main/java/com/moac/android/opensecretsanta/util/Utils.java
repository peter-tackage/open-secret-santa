package com.moac.android.opensecretsanta.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.moac.android.opensecretsanta.activity.ContactModes;
import com.moac.android.opensecretsanta.activity.ShareResults;
import com.moac.android.opensecretsanta.model.Member;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Utils {

    public static final String TAG = Utils.class.getSimpleName();

    private static final String DO_ONCE_TAG = "do_once";

    public static boolean doOnce(Context _context, String _taskTag, Runnable _task) {
        final String prefTag = DO_ONCE_TAG + _taskTag;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        boolean isDone = prefs.getBoolean(prefTag, false);
        if (!isDone) {
            _task.run();
            prefs.edit().putBoolean(prefTag, true).commit();
            return true;
        }
        return false;
    }

    public static void safeClose(InputStream _stream) {
        if(_stream == null)
            return;
        try {
            _stream.close();
        } catch(IOException e) { }
    }

    public static String buildPersonalisedMsg(String extraMsg, String from, String to) {
        Log.v(TAG, "buildPersonalisedMsg() - start");

        StringBuilder s = new StringBuilder();
        s.append("Hi ");
        s.append(from);
        s.append(", Open Secret Santa has assigned you: ");
        s.append(to);
        s.append(".\n");
        s.append(extraMsg == null ? "" : extraMsg);

        Log.v(TAG, "buildPersonalisedMsg() - result: " + s.toString());

        return s.toString();
    }

    public static String buildSuccessMessage(ShareResults result) {

        // TODO Change for different model.
        StringBuilder s = new StringBuilder();
        //s.append(result.sentSMSCount);
        s.append("Messages sent to ");
        s.append(result.sentRecipientCount);
        if(result.sentRecipientCount > 1) {
            s.append(" members");
        } else {
            s.append(" member");
        }

        Log.v(TAG, "buildPersonalisedMsg() - result: " + s.toString());

        return s.toString();
    }

    public static boolean containsSendableEntry(List<Member> _members) {
        for(Member member : _members) {
            if(isSendable(member))
                return true;
        }
        return false;
    }

    public static boolean containsEmailSendableEntry(List<Member> _members) {
        for(Member member : _members) {
            if(member.getContactMode() == ContactModes.EMAIL_CONTACT_MODE)
                return true;
        }
        return false;
    }

    public static int getShareableCount(List<Member> _members) {
        int count = 0;
        for(Member member : _members) {
            if(isSendable(member))
                count++;
        }
        return count;
    }

    public static boolean isSendable(Member _member) {
        return _member.getContactMode() != ContactModes.NAME_ONLY_CONTACT_MODE;
    }
}
