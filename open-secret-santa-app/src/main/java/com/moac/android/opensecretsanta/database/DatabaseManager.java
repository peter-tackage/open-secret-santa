package com.moac.android.opensecretsanta.database;
/*
*
* Derived from the Feeder app.
*
* Copyright (C) 2012 Stoyan Rachev (stoyanr@gmail.com)
*
* This program is free software; you can redistribute it and/or modify it
* under the terms of the GNU General Public License as published by the
* Free Software Foundation; either version 2, or (at your option) any
* later version.
*
* This program is distributed in the hope that it will be useful, but
* WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* General Public License for more details.
*/

import android.database.SQLException;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.PersistableObject;
import com.moac.android.opensecretsanta.model.Restriction;

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

    @SuppressWarnings("unchecked")
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

    public List<Restriction> queryAllRestrictionsForMemberId(long memberId) {
        try {
            return mDbHelper.getDaoEx(Restriction.class).queryBuilder()
                    .where().eq(Restriction.Columns.MEMBER_ID_COLUMN, memberId)
                    .query();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public boolean queryIsRestricted(long fromMemberId, long toMemberId) {
        try {
            Restriction restriction = mDbHelper.getDaoEx(Restriction.class).queryBuilder()
                    .where().eq(Restriction.Columns.MEMBER_ID_COLUMN, fromMemberId)
                    .and().eq(Restriction.Columns.OTHER_MEMBER_ID_COLUMN, toMemberId)
                    .queryForFirst();
            return restriction != null;
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public List<Assignment> queryAllAssignmentsForGroup(long groupId) {
        try {
            QueryBuilder<Member, Long> groupMembersQuery =
                    mDbHelper.getDaoEx(Member.class).queryBuilder();
            groupMembersQuery.selectColumns(Member.Columns._ID).where().eq(Member.Columns.GROUP_ID_COLUMN, groupId);

            return mDbHelper.getDaoEx(Assignment.class).queryBuilder()
                    .where().in(Assignment.Columns.GIVER_MEMBER_ID_COLUMN, groupMembersQuery)
                    .query();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public boolean queryHasAssignmentsForGroup(long groupId) {
        try {
            QueryBuilder<Member, Long> groupMembersQuery =
                    mDbHelper.getDaoEx(Member.class).queryBuilder();
            groupMembersQuery.selectColumns(Member.Columns._ID).where().eq(Member.Columns.GROUP_ID_COLUMN, groupId);

            return mDbHelper.getDaoEx(Assignment.class).queryBuilder()
                    .where().in(Assignment.Columns.GIVER_MEMBER_ID_COLUMN, groupMembersQuery)
                    .queryForFirst() != null;
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public Assignment queryAssignmentForMember(long _memberId) {
        try {
            QueryBuilder<Assignment, Long> assignmentQuery =
                    mDbHelper.getDaoEx(Assignment.class).queryBuilder();

            assignmentQuery.where().eq(Assignment.Columns.GIVER_MEMBER_ID_COLUMN, _memberId);
            return assignmentQuery.queryForFirst();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public List<Member> queryAllMembersForGroup(long groupId) {
        try {
            return mDbHelper.getDaoEx(Member.class).queryBuilder()
                    .where().eq(Member.Columns.GROUP_ID_COLUMN, groupId)
                    .query();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public List<Member> queryAllMembersForGroupExcept(long groupId, long exceptMemberId) {
        try {
            return mDbHelper.getDaoEx(Member.class).queryBuilder()
                    .where().eq(Member.Columns.GROUP_ID_COLUMN, groupId)
                    .and().ne(Member.Columns._ID, exceptMemberId)
                    .query();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public Member queryMemberWithNameForGroup(long groupId, String name) {
        try {
            // Use SelectArg to ensure values are properly escaped
            // Refer - http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_3.html#index-select-arguments
            SelectArg selectArg = new SelectArg();
            selectArg.setValue(name);
            return mDbHelper.getDaoEx(Member.class).queryBuilder()
                    .where().eq(Member.Columns.GROUP_ID_COLUMN, groupId)
                    .and()
                    .like(Member.Columns.NAME_COLUMN, selectArg)
                    .queryForFirst();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public long deleteAllRestrictionsForMember(long memberId) {
        try {
            DeleteBuilder<Restriction, Long> deleteBuilder = mDbHelper.getDaoEx(Restriction.class).deleteBuilder();
            deleteBuilder.where().eq(Restriction.Columns.MEMBER_ID_COLUMN, memberId);
            return deleteBuilder.delete();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public long deleteRestrictionBetweenMembers(long fromMember, long otherMember) {
        try {
            DeleteBuilder<Restriction, Long> deleteBuilder =
                    mDbHelper.getDaoEx(Restriction.class).deleteBuilder();
            deleteBuilder.where()
                    .eq(Restriction.Columns.MEMBER_ID_COLUMN, fromMember).
                    and().
                    eq(Restriction.Columns.OTHER_MEMBER_ID_COLUMN, otherMember);
            return deleteBuilder.delete();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public long deleteAllAssignmentsForGroup(long groupId) {
        try {
            QueryBuilder<Member, Long> groupMembersQuery =
                    mDbHelper.getDaoEx(Member.class).queryBuilder();
            groupMembersQuery.selectColumns(Member.Columns._ID).where().eq(Member.Columns.GROUP_ID_COLUMN, groupId);

            DeleteBuilder<Assignment, Long> deleteBuilder = mDbHelper.getDaoEx(Assignment.class).deleteBuilder();
            deleteBuilder.where().in(Assignment.Columns.GIVER_MEMBER_ID_COLUMN, groupMembersQuery);
            return deleteBuilder.delete();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public boolean queryHasGroup() {
        try {
            Group group = mDbHelper.getDaoEx(Group.class).queryBuilder()
                    .queryForFirst();
            return group != null;
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public Group queryForFirstGroup() {
        try {
            return mDbHelper.getDaoEx(Group.class).queryBuilder()
                    .queryForFirst();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public Group queryForGroupWithName(String groupName) {
        try {
            // Use SelectArg to ensure values are properly escaped
            // Refer - http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_3.html#index-select-arguments
            SelectArg selectArg = new SelectArg();
            selectArg.setValue(groupName);
            return mDbHelper.getDaoEx(Group.class).queryBuilder()
                    .where()
                    .like(Group.Columns.NAME_COLUMN, selectArg)
                    .queryForFirst();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public int updateAllAssignmentsInGroup(long groupId, Assignment.Status status) {
        try {
            QueryBuilder<Member, Long> groupMembersQuery =
                    mDbHelper.getDaoEx(Member.class).queryBuilder();
            groupMembersQuery.selectColumns(Member.Columns._ID).where().eq(Member.Columns.GROUP_ID_COLUMN, groupId);

            UpdateBuilder<Assignment, Long> updateBuilder =
                    mDbHelper.getDaoEx(Assignment.class).updateBuilder();
            updateBuilder.updateColumnValue(Assignment.Columns.SEND_STATUS_COLUMN, status).
                    where().in(Assignment.Columns.GIVER_MEMBER_ID_COLUMN, groupMembersQuery);
            return updateBuilder.update();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public int updateGroupName(long groupId, String groupName) {
        try {
            // Escape the group name
            SelectArg selectArg = new SelectArg();
            selectArg.setValue(groupName);
            UpdateBuilder<Group, Long> updateBuilder =
                    mDbHelper.getDaoEx(Group.class).updateBuilder();
            updateBuilder.updateColumnValue(Group.Columns.NAME_COLUMN, selectArg).
                    where().eq(Group.Columns._ID, groupId);
            return updateBuilder.update();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public <T extends PersistableObject> long queryMaxId(Class<T> objClass) {
        try {
            PersistableObject obj = mDbHelper.getDaoEx(objClass).queryBuilder()
                    .orderBy(T.Columns._ID, false).queryForFirst();
            return obj == null ? 0 : obj.getId();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

}
