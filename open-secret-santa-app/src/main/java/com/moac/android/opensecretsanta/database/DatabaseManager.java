package com.moac.android.opensecretsanta.database;

import android.database.SQLException;
import com.j256.ormlite.stmt.DeleteBuilder;
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
     * Bespoke queries
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
        try {
            return mDbHelper.getDaoEx(DrawResult.class).queryBuilder()
              .orderBy(DrawResult.Columns.DRAW_DATE_COLUMN, false)
              .where().eq(DrawResult.Columns.GROUP_ID_COLUMN, groupId)
              .queryForFirst();
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public List<Restriction> queryAllRestrictionsForMemberId(long memberId) {
        try {
            return mDbHelper.getDaoEx(Restriction.class).queryBuilder()
              .where().eq(Restriction.Columns.MEMBER_ID_COLUMN, memberId)
              .query();
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public List<DrawResultEntry> queryAllDrawResultEntriesForDrawId(long drawResultId) {
        try {
            return mDbHelper.getDaoEx(DrawResultEntry.class).queryBuilder()
              .where().eq(DrawResultEntry.Columns.DRAW_RESULT_ID_COLUMN, drawResultId)
              .query();
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public List<DrawResult> queryAllDrawResultsForGroup(long groupId) {
        try {
            return mDbHelper.getDaoEx(DrawResult.class).queryBuilder()
              .where().eq(DrawResult.Columns.GROUP_ID_COLUMN, groupId)
              .query();
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public List<Member> queryAllMembersForGroup(long groupId) {
        try {
            return mDbHelper.getDaoEx(Member.class).queryBuilder()
              .where().eq(Member.Columns.GROUP_ID_COLUMN, groupId)
              .query();
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public Member queryMemberWithNameForGroup(long groupId, String name) {
        try {
            return mDbHelper.getDaoEx(Member.class).queryBuilder()
              .where().eq(Member.Columns.GROUP_ID_COLUMN, groupId)
              .and()
              .eq(Member.Columns.NAME_COLUMN, name)
              .queryForFirst();
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public void deleteAllRestrictionsForMember(long memberId) {
        try {
            DeleteBuilder<Restriction, Long> deleteBuilder = mDbHelper.getDaoEx(Restriction.class).deleteBuilder();
            deleteBuilder.where().eq(Restriction.Columns.MEMBER_ID_COLUMN, memberId);
            deleteBuilder.delete();
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }
}
