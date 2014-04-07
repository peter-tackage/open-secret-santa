package com.moac.android.opensecretsanta.notify;

public class NotificationFailureException extends Exception {
    public NotificationFailureException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
