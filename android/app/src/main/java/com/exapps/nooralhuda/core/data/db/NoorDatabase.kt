package com.exapps.nooralhuda.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** v2 adds bookmark tombstones for delete propagation. */
@Database(entities = [ContentCacheEntry::class, BookmarkEntity::class], version = 2)
abstract class NoorDatabase : RoomDatabase() {
    abstract fun contentCacheDao(): ContentCacheDao
    abstract fun bookmarkDao(): BookmarkDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE bookmarks ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
    }
}
