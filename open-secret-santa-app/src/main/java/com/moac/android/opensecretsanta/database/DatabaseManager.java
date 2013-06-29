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
import com.moac.android.opensecretsanta.model.Assignment;
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
        } catch(java.sql.SQLException e) {
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
        } catch(java.sql.SQLException e) {
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
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public Assignment queryAssignmentForMember(long _memberId) {
        try {
            QueryBuilder<Assignment, Long> assignmentQuery =
              mDbHelper.getDaoEx(Assignment.class).queryBuilder();

            assignmentQuery.selectColumns(Member.Columns._ID).where().eq(Assignment.Columns.GIVER_MEMBER_ID_COLUMN, _memberId);
            return assignmentQuery.queryForFirst();

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

    public List<Member> queryAllMembersForGroupExcept(long groupId, long exceptMemberId) {
        try {
            return mDbHelper.getDaoEx(Member.class).queryBuilder()
              .where().eq(Member.Columns.GROUP_ID_COLUMN, groupId)
              .and().ne(Member.Columns._ID, exceptMemberId)
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

    public int deleteAllRestrictionsForMember(long memberId) {
        try {
            DeleteBuilder<Restriction, Long> deleteBuilder = mDbHelper.getDaoEx(Restriction.class).deleteBuilder();
            deleteBuilder.where().eq(Restriction.Columns.MEMBER_ID_COLUMN, memberId);
            return deleteBuilder.delete();
        } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public int deleteAllAssignmentsForGroup(long groupId) {
        try {
            QueryBuilder<Member, Long> groupMembersQuery =
              mDbHelper.getDaoEx(Member.class).queryBuilder();
            groupMembersQuery.selectColumns(Member.Columns._ID).where().eq(Member.Columns.GROUP_ID_COLUMN, groupId);

            DeleteBuilder<Assignment, Long> deleteBuilder = mDbHelper.getDaoEx(Assignment.class).deleteBuilder();
            deleteBuilder.where().in(Assignment.Columns.GIVER_MEMBER_ID_COLUMN, groupMembersQuery);
            return deleteBuilder.delete();
            } catch(java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }
}
