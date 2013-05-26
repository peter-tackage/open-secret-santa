package com.moac.android.opensecretsanta.test.builders;

import com.moac.android.opensecretsanta.types.Group;
import com.moac.android.opensecretsanta.types.PersistableObject;

public class GroupBuilder {

    private long id = PersistableObject.UNSET_ID; // uninserted.
    private boolean isReady = false; // should match default
    private String name = "groupName1";

    public GroupBuilder withReady(boolean isReady) {
        this.isReady = isReady;
        return this;
    }

    public GroupBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public Group build() {
        Group group = new Group();
        group.setId(id);
        group.setReady(isReady);
        group.setName(name);
        return group;
    }
}
