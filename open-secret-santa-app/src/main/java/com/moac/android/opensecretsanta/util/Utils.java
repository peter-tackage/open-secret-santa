package com.moac.android.opensecretsanta.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.moac.android.opensecretsanta.model.ContactMethod;
import com.moac.android.opensecretsanta.model.Member;

import java.util.List;

public class Utils {

    public static final String TAG = Utils.class.getSimpleName();

    private static final String DO_ONCE_TAG = "do_once";

    public static boolean doOnce(Context _context, String _taskTag, Runnable _task) {
        final String prefTag = DO_ONCE_TAG + _taskTag;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        boolean isDone = prefs.getBoolean(prefTag, false);
        if(!isDone) {
            _task.run();
            prefs.edit().putBoolean(prefTag, true).commit();
            return true;
        }
        return false;
    }

// TODO Fix this
//    public static String buildSuccessMessage(ShareResults result) {
//
//        // TODO Change for different model.
//        StringBuilder s = new StringBuilder();
//        //s.append(result.sentSMSCount);
//        s.append("Messages sent to ");
//        s.append(result.sentRecipientCount);
//        if(result.sentRecipientCount > 1) {
//            s.append(" members");
//        } else {
//            s.append(" member");
//        }
//
//        Log.v(TAG, "buildMsg() - result: " + s.toString());
//
//        return s.toString();
//    }

    public static boolean containsSendableEntry(List<Member> _members) {
        for(Member member : _members) {
            if(member.getContactMode().isSendable()) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsEmailSendableEntry(List<Member> _members) {
        for(Member member : _members) {
            if(member.getContactMode() == ContactMethod.EMAIL)
                return true;
        }
        return false;
    }

    public static int getShareableCount(List<Member> _members) {
        int count = 0;
        for(Member member : _members) {
            if(member.getContactMode().isSendable()) {
                count++;
            }
        }
        return count;
    }
}
