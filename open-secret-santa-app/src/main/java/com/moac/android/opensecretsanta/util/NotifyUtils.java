package com.moac.android.opensecretsanta.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.provider.Telephony;

import com.moac.android.opensecretsanta.BuildConfig;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.ContactMethod;
import com.moac.android.opensecretsanta.model.Member;

import java.util.List;

public class NotifyUtils {

    public static boolean containsSendableEntry(List<Member> _members) {
        for (Member member : _members) {
            if (member.getContactMethod().isSendable()) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsEmailSendableEntry(List<Member> _members) {
        for (Member member : _members) {
            if (member.getContactMethod() == ContactMethod.EMAIL) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsEmailSendableEntry(DatabaseManager db, long[] _memberIds) {
        for (long id : _memberIds) {
            Member member = db.queryById(id, Member.class);
            if (member.getContactMethod() == ContactMethod.EMAIL) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsSmsSendableEntry(DatabaseManager db, long[] _memberIds) {
        for (long id : _memberIds) {
            Member member = db.queryById(id, Member.class);
            if (member.getContactMethod() == ContactMethod.SMS) {
                return true;
            }
        }
        return false;
    }

    private boolean isSendable(Member member) {
        return (member != null && member.getContactMethod() != null) && member.getContactMethod().isSendable();
    }

    public static int getShareableCount(List<Member> _members) {
        int count = 0;
        for (Member member : _members) {
            if (member.getContactMethod().isSendable()) {
                count++;
            }
        }
        return count;
    }

    public static boolean requiresSmsPermission(Context context, DatabaseManager db, long[] memberIds) {
        return requiresDefaultSmsCheck() && !isDefaultSmsApp(context) && containsSmsSendableEntry(db, memberIds);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isDefaultSmsApp(Context context) {
        // Pre-Kitkat report that we are the default - we have equivalent privileges
        if (!requiresDefaultSmsCheck()) return true;

        String currentDefaultApp = Telephony.Sms.getDefaultSmsPackage(context);
        return currentDefaultApp != null && currentDefaultApp.equals(BuildConfig.APPLICATION_ID);
    }

    public static boolean requiresDefaultSmsCheck() {
        // Pre Kitkat, we don't have the concept of Default SMS apps
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT);
    }
}
