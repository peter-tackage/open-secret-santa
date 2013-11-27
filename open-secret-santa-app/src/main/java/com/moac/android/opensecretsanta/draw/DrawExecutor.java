package com.moac.android.opensecretsanta.draw;

import com.moac.android.opensecretsanta.model.Group;
import com.moac.drawengine.DrawEngine;
import rx.Observable;

public interface DrawExecutor {
    public Observable<DrawResultEvent> requestDraw(DrawEngine _engine, Group _group);
}
