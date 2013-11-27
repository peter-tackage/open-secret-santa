package com.moac.android.opensecretsanta.util;

import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Group;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GroupUtils {

    public static Group createIncrementingGroup(DatabaseManager db, String baseName) {
        // TODO Should really synchronize
        long now = System.currentTimeMillis();
        long currentMaxId = db.queryMaxId(Group.class);
        Group group = new Group();

        String name = GroupUtils.generateGroupName(baseName,
          new SimpleDateFormat("yyyy"),
          new Date(now), currentMaxId + 1);
        group.setName(name);
        group.setCreatedAt(now);

        // Create the group
        db.create(group);
        return group;
    }

    // Ids definitely start at 1 - See http://www.sqlite.org/autoinc.html
    private static String generateGroupName(String baseName, SimpleDateFormat formatter, Date date, long id) {
        StringBuilder sb = new StringBuilder();
        sb.append(baseName);
        sb.append(" ");
        String suffix = formatter.format(date);
        sb.append(suffix);
        sb.append(" #");
        sb.append(id);
        return sb.toString();
    }

}
