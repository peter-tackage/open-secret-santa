package com.moac.android.opensecretsanta.util;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context);
            return !defaultSmsApp.equals(BuildConfig.PACKAGE_NAME)
                    && containsSmsSendableEntry(db, memberIds);
        } else {
            return false;
        }
    }
}
