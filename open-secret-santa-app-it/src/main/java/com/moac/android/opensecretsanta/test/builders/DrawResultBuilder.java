package com.moac.android.opensecretsanta.test.builders;

import com.moac.android.opensecretsanta.activity.Constants;
import com.moac.android.opensecretsanta.types.DrawResult;

import java.util.Date;

public class DrawResultBuilder {

    private long id = -1; // uninserted.
    private long drawDate = Date.parse("1 August, 2011");
    private long sendDate = Constants.UNSENT_DATE;
    private String msg = "A simple test message.";

    public DrawResultBuilder withId(long id) {
        this.id = id;
        return this;
    }

    public DrawResultBuilder withDrawDate(long drawDate) {
        this.drawDate = drawDate;
        return this;
    }

    public DrawResultBuilder withSendDate(long sendDate) {
        this.sendDate = sendDate;
        return this;
    }

    public DrawResultBuilder withMessage(String msg) {
        this.msg = msg;
        return this;
    }

    public DrawResult build() {
        return new DrawResult(this.id, this.drawDate, this.sendDate, this.msg);
    }
}
