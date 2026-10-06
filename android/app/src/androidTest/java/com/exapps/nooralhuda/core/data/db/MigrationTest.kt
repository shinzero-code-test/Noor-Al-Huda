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

    @Test
    fun migrate2To3_createsQuranTables() {
        helper.createDatabase(TEST_DB, 2).close()
        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_2_3)
        val cursor = db.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name IN ('surahs', 'verses', 'reciter_downloads')"
        )
        val tables = mutableListOf<String>()
        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0))
        }
        cursor.close()
        db.close()
        assertEquals(setOf("surahs", "verses", "reciter_downloads"), tables.toSet())
    }

    @Test
    fun migrate3To4_createsPrayerTables() {
        helper.createDatabase(TEST_DB, 3).close()
        val db = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4)
        val cursor = db.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name IN ('prayer_days', 'worship_log', 'azkar_entries')"
        )
        val tables = mutableListOf<String>()
        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0))
        }
        cursor.close()
        db.close()
        assertEquals(setOf("prayer_days", "worship_log", "azkar_entries"), tables.toSet())
    }

    @Test
    fun migrate4To5_createsHadithTables() {
        helper.createDatabase(TEST_DB, 4).close()
        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5)
        val cursor = db.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' " +
                "AND name IN ('hadith_collections', 'hadith_items', 'hadith_details')"
        )
        val tables = mutableListOf<String>()
        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0))
        }
        cursor.close()
        db.close()
        assertEquals(
            setOf("hadith_collections", "hadith_items", "hadith_details"),
            tables.toSet()
        )
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
