package com.moac.android.opensecretsanta.test.builders;

import com.moac.android.opensecretsanta.types.Group;

public class GroupBuilder {

    private String name = "groupName1";

    public GroupBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public Group build() {
        Group group = new Group();
        group.setName(name);
        return group;
    }
}
