package com.moac.android.opensecretsanta.types;

import android.util.Log;

import java.util.List;

public class Utilities {

    public static final String TAG = "Utilities";

    public static String buildSharedErrorMessage(List<DrawResultEntry> failedEntries) {
        StringBuilder s = new StringBuilder();
        s.append("Could not notify the following member(s) : ");
        for(DrawResultEntry m : failedEntries) {
            s.append(m.getGiverName());
            s.append(" (");
            s.append(m.getContactDetail());
            s.append(")");
            if(failedEntries.indexOf(m) != (failedEntries.size() - 1)) {
                s.append(", ");
            }
        }
        return s.toString();
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

        // TODO Change for different types.
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

    public static boolean containsSendableEntry(List<DrawResultEntry> _entries) {
        for(DrawResultEntry entry : _entries) {
            if(entry.isSendable())
                return true;
        }
        return false;
    }

    public static int getShareableCount(List<DrawResultEntry> _entries) {
        int count = 0;
        for(DrawResultEntry entry : _entries) {
            if(entry.isSendable())
                count++;
        }
        return count;
    }
}
