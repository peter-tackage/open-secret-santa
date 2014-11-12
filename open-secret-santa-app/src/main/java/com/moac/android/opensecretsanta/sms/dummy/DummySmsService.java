package com.moac.android.opensecretsanta.sms.dummy;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DummySmsService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
