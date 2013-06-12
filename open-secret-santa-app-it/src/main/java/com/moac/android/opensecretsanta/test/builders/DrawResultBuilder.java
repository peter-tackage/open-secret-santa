package com.moac.android.opensecretsanta.test.builders;

import com.moac.android.opensecretsanta.types.DrawResult;

import java.util.Date;

public class DrawResultBuilder {

    @SuppressWarnings("deprecation")
    private long drawDate = Date.parse("1 August, 2011");

    public DrawResultBuilder withDrawDate(long drawDate) {
        this.drawDate = drawDate;
        return this;
    }

    public DrawResult build() {
        DrawResult dr = new DrawResult();
        dr.setDrawDate(drawDate);
        return dr;
    }
}
