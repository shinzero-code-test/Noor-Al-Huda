package com.exapps.nooralhuda.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/** v1. Typed tables grow per slice; every version gets an explicit migration + test. */
@Database(entities = [ContentCacheEntry::class, BookmarkEntity::class], version = 1)
abstract class NoorDatabase : RoomDatabase() {
    abstract fun contentCacheDao(): ContentCacheDao
    abstract fun bookmarkDao(): BookmarkDao
}
