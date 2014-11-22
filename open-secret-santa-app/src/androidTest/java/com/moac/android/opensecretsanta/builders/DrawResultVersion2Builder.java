package com.moac.android.opensecretsanta.builders;

import com.moac.android.opensecretsanta.model.version2.DrawResultVersion2;
import com.moac.android.opensecretsanta.model.version2.GroupVersion2;

public class DrawResultVersion2Builder {

    private long mDrawDate;
    private long mSendDate;
    private String mMessage;
    private GroupVersion2 mGroup;

    public DrawResultVersion2Builder withDrawDate(long drawDate) {
        mDrawDate = drawDate;
        return this;
    }

    public DrawResultVersion2Builder withSendDate(long sendDate) {
        mSendDate = sendDate;
        return this;
    }

    public DrawResultVersion2Builder withMessage(String message) {
        mMessage = message;
        return this;
    }

    public DrawResultVersion2Builder withGroup(GroupVersion2 group) {
        mGroup = group;
        return this;
    }

    public DrawResultVersion2 build() {
        DrawResultVersion2 drawResult = new DrawResultVersion2();
        drawResult.setMessage(mMessage);
        drawResult.setDrawDate(mDrawDate);
        drawResult.setSendDate(mSendDate);

        // build default required Group
        if(mGroup == null) {
            GroupVersion2Builder gBuilder = new GroupVersion2Builder();
            mGroup = gBuilder.build();
        }
        drawResult.setGroup(mGroup);
        return drawResult;
    }
}
