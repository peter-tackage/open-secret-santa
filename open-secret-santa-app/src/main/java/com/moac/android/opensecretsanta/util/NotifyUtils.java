package com.moac.android.opensecretsanta.util;

import com.moac.android.opensecretsanta.model.ContactMode;
import com.moac.android.opensecretsanta.model.Member;

import java.util.List;

public class NotifyUtils {

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
            if(member.getContactMode() == ContactMode.EMAIL) {
                return true;
            }
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
