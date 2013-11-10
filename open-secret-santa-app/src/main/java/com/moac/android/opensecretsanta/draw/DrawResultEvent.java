package com.moac.android.opensecretsanta.draw;

import java.util.Map;

public class DrawResultEvent {

    private final long mGroupId;
    private Exception mException;
    private String mMsg;
    private Map<Long, Long> mAssignments;

    DrawResultEvent(long groupId) {
        mGroupId = groupId;
    }

    public Exception getException() {
        return mException;
    }

    void setException(Exception exception) {
        mException = exception;
    }

    public String getMsg() {
        return mMsg;
    }

    void setMsg(String msg) {
        mMsg = msg;
    }

    public Map<Long, Long> getAssignments() {
        return mAssignments;
    }

    void setAssignments(Map<Long, Long> assignments) {
        mAssignments = assignments;
    }

    public long getGroupId() {
        return mGroupId;
    }

    public boolean isSuccess() {
        return mAssignments != null && mException == null;
    }

    public static DrawResultEvent failure(long groupId, Exception exception) {
        DrawResultEvent result = new DrawResultEvent(groupId);
        result.setException(exception);
        return result;
    }

    public static DrawResultEvent failure(long groupId, Exception exception, String msg) {
        DrawResultEvent result = new DrawResultEvent(groupId);
        result.setException(exception);
        result.setMsg(msg);
        return result;
    }

    public static DrawResultEvent success(long groupId, Map<Long, Long> assignment) {
        DrawResultEvent result = new DrawResultEvent(groupId);
        result.setAssignments(assignment);
        return result;
    }
}
