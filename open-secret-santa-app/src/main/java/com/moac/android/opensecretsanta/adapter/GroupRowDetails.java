package com.moac.android.opensecretsanta.adapter;

import com.moac.android.opensecretsanta.model.Member;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GroupRowDetails {

    private static final SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy");

    protected long mId;
    protected String mName;
    protected long mCreationDate;
    protected List<Member> mMembers;

    public GroupRowDetails(long _groupId, String _groupName, long _groupCreationDate, List<Member> _groupMembers) {
        mId = _groupId;
        mName = _groupName;
        mCreationDate = _groupCreationDate;
        mMembers = _groupMembers;
    }

    public long getId() { return mId; }

    public String getName() { return mName; }

    public String getCreationDate() {
        Date date = new Date(mCreationDate);
        return format.format(date);
    }

    public List<Member> getMembers() { return mMembers; }
}
