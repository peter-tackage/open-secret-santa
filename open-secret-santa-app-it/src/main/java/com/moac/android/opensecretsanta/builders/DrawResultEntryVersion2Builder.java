package com.moac.android.opensecretsanta.builders;

import com.moac.android.opensecretsanta.model.version2.ConstantsVersion2;
import com.moac.android.opensecretsanta.model.version2.DrawResultEntryVersion2;
import com.moac.android.opensecretsanta.model.version2.DrawResultVersion2;

public class DrawResultEntryVersion2Builder {

    public final static String TEST_GIVER_NAME = "testGiver";
    public final static String TEST_RECEIVER_NAME = "testReceiver";
    public final static String TEST_DRE_CONTACT_DETAILS = "+1191191@";

    private long mDrawResultId = DrawResultEntryVersion2.UNSET_ID;
    private String mGiverName = TEST_GIVER_NAME;
    private String mReceiverName = TEST_RECEIVER_NAME;
    private int mContactMode = ConstantsVersion2.NAME_ONLY_CONTACT_MODE;
    private String mContactDetails = TEST_DRE_CONTACT_DETAILS;
    private long mViewedDate;
    private long mSentDate;
    private DrawResultVersion2 mDrawResult;

    public DrawResultEntryVersion2Builder withDrawResultId(long drawResultId) {
        mDrawResultId = drawResultId;
        return this;
    }

    public DrawResultEntryVersion2Builder withGiverName(String giverName) {
        mGiverName = giverName;
        return this;
    }

    public DrawResultEntryVersion2Builder withReceiverName(String receiverName) {
        mReceiverName = receiverName;
        return this;
    }

    public DrawResultEntryVersion2Builder withContactMode(int contactMode) {
        mContactMode = contactMode;
        return this;
    }

    public DrawResultEntryVersion2Builder withContactDetails(String contactDetails) {
        mContactDetails = contactDetails;
        return this;
    }

    public DrawResultEntryVersion2Builder withViewedDate(long viewedDate) {
        mViewedDate = viewedDate;
        return this;
    }

    public DrawResultEntryVersion2Builder withSentDate(long sentDate) {
        mSentDate = sentDate;
        return this;
    }

    public DrawResultEntryVersion2Builder withDrawResult(DrawResultVersion2 drawResult) {
        mDrawResult = drawResult;
        return this;
    }

    public DrawResultEntryVersion2 build() {
        DrawResultEntryVersion2 drawResultEntry = new DrawResultEntryVersion2();
        drawResultEntry.setGiverName(mGiverName);
        drawResultEntry.setReceiverName(mReceiverName);
        drawResultEntry.setContactMode(mContactMode);
        drawResultEntry.setContactDetail(mContactDetails);
        drawResultEntry.setViewedDate(mViewedDate);
        drawResultEntry.setSentDate(mSentDate);

        // build default required DrawResult
        if(mDrawResult == null) {
            DrawResultVersion2Builder drBuilder = new DrawResultVersion2Builder();
            mDrawResult = drBuilder.build();
        }
        drawResultEntry.setDrawResult(mDrawResult);
        return drawResultEntry;
    }
}
