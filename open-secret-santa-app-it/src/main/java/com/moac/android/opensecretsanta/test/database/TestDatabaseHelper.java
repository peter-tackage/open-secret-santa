package com.moac.android.opensecretsanta.test.database;

import android.content.Context;
import com.moac.android.opensecretsanta.database.DatabaseHelper;

public class TestDatabaseHelper extends DatabaseHelper {

    private static final String TEST_DATABASE_NAME = "testopensecretsanta.db";

    protected TestDatabaseHelper(Context context) {
        super(context, TEST_DATABASE_NAME);
    }
}
