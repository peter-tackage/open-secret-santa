package com.moac.android.opensecretsanta.database;

import android.database.SQLException;
import com.moac.android.opensecretsanta.types.*;

import java.util.List;

public class DatabaseManager {

    private final DatabaseHelper mDbHelper;

    public DatabaseManager(DatabaseHelper helper) {
        mDbHelper = helper;
    }

    public <T extends PersistableObject> List<T> queryAll(Class<T> objClass) {
        return mDbHelper.queryAll(objClass);
    }

    public <T extends PersistableObject> T queryById(long id, Class<T> objClass) {
        return mDbHelper.queryById(id, objClass);
    }

    public <T extends PersistableObject> T queryById(T obj) {
        return (T) mDbHelper.queryById(obj.getId(), obj.getClass());
    }

    public <T extends PersistableObject> long create(T obj) {
        return mDbHelper.create(obj, obj.getClass());
    }

    public <T extends PersistableObject> void update(T obj) {
        mDbHelper.update(obj, obj.getClass());
    }

    public <T extends PersistableObject> void delete(T obj) {
        mDbHelper.deleteById(obj.getId(), obj.getClass());
    }

    public <T extends PersistableObject> void delete(long id, Class<T> objClass) {
        mDbHelper.deleteById(id, objClass);
    }

    /**
     * Groups
     */

    public List<Group> queryAllGroupsWhereReady() {
        try {
            return mDbHelper.getDaoEx(Group.class).queryBuilder()
              .where().eq(Group.Columns.IS_READY, true)
              .query();
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public DrawResult queryLatestDrawResultForGroup(long groupId) {
      // TODO Implement
        return  null;
    }

    public List<Restriction> queryAllRestrictionsForMemberId(long memberId) {
        // TODO Implement
        return  null;
    }

    public List<DrawResultEntry> queryAllDrawResultEntriesForDrawId(long id) {
        // TODO Implement
        return  null;
    }

    public List<DrawResult> queryAllDrawResultsForGroup(long groupId) {
        // TODO Implement
        return  null;
    }

    public List<Member> queryAllMembersForGroup(long groupId) {
        // TODO Implement
        return  null;
    }

    public Member queryMemberWithNameForGroup(long groupId, String name) {
        // TODO Implement
        return  null;
    }

    public void deleteAllRestrictionsForMember(long memberId) {
        // TODO Implement
    }
}
