package com.moac.android.opensecretsanta.builders;

import com.moac.android.opensecretsanta.model.version2.GroupVersion2;

public class GroupVersion2Builder {

    public static String TEST_GROUP_NAME = "groupName";
    String mName = TEST_GROUP_NAME;
    boolean mIsReady = true;

    public GroupVersion2Builder withName(String name) {
        mName = name;
        return this;
    }

    public GroupVersion2Builder withIsReady(boolean isReady) {
        mIsReady = isReady;
        return this;
    }

    public GroupVersion2 build() {
        GroupVersion2 group = new GroupVersion2();
        group.setName(mName);
        group.setReady(mIsReady);
        return group;
    }
}
