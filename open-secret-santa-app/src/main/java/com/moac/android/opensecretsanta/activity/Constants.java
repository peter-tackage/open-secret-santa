package com.moac.android.opensecretsanta.activity;

public class Constants {

    public static final String MEMBER_ID = "memberId";
    public static final String MEMBER_NAME = "memberName";

    public static final String GROUP_NAME = "groupName";
    public static final String GROUP_ID = "groupId";

    public static final String DRAW_RESULT_ID = "drawResultId";
    public static final String MOBILE_NUMBERS_LIST_ID = "mobileNumbersListId";

    /*
     * Contact Types
     */
    public static final int NAME_ONLY_CONTACT_MODE = 0;
    public static final int SMS_CONTACT_MODE = 1;
    public static final int EMAIL_CONTACT_MODE = 2;

    /*
     * DrawResultEntry Dates
     */
    public static final long UNVIEWED_DATE = 0;
    public static final long UNSENT_DATE = 0;

    /*
     * DrawResult Dates
     */
    public static final long UNDRAWN_DATE = 0;
    public static final int MAX_MESSAGE_SIZE = 500;

    public static final String ACCOUNT_TYPE_GOOGLE = "com.google";
    public static final String[] FEATURES_MAIL = { "service_mail" };
    public static final String GMAIL_TOKEN_TYPE =  "oauth2:https://mail.google.com/";

}
