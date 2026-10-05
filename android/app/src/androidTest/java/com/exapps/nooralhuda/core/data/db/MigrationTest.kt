package com.exapps.nooralhuda.core.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Every schema version gets a migration test. v1 is the base (create 1, migrate to latest). */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        NoorDatabase::class.java
    )

    @Test
    fun migrate1To2_addsDeletedColumn() {
        helper.createDatabase(TEST_DB, 1).close()
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
        val cursor = db.query("PRAGMA table_info(bookmarks)")
        val columns = mutableListOf<String>()
        while (cursor.moveToNext()) {
            columns.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
        }
        cursor.close()
        db.close()
        assertEquals(true, columns.contains("deleted"))
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
