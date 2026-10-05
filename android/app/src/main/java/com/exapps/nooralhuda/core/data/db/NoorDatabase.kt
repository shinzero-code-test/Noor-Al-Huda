package com.exapps.nooralhuda.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.exapps.nooralhuda.feature.quran.data.ReciterDownloadDao
import com.exapps.nooralhuda.feature.quran.data.ReciterDownloadEntity
import com.exapps.nooralhuda.feature.quran.data.SurahDao
import com.exapps.nooralhuda.feature.quran.data.SurahEntity
import com.exapps.nooralhuda.feature.quran.data.VerseDao
import com.exapps.nooralhuda.feature.quran.data.VerseEntity

/** v3 adds the Quran corpus tables (surahs, verses, reciter downloads). */
@Database(
    entities = [
        ContentCacheEntry::class,
        BookmarkEntity::class,
        SurahEntity::class,
        VerseEntity::class,
        ReciterDownloadEntity::class
    ],
    version = 3
)
abstract class NoorDatabase : RoomDatabase() {
    abstract fun contentCacheDao(): ContentCacheDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun surahDao(): SurahDao
    abstract fun verseDao(): VerseDao
    abstract fun reciterDownloadDao(): ReciterDownloadDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE bookmarks ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS surahs (" +
                "id INTEGER NOT NULL PRIMARY KEY, arabic TEXT NOT NULL, " +
                "transliteration TEXT NOT NULL, english TEXT NOT NULL, " +
                "verses INTEGER NOT NULL, revelation TEXT NOT NULL, updatedAt INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS verses (" +
                "surahId INTEGER NOT NULL, number INTEGER NOT NULL, " +
                "arabic TEXT NOT NULL, tajweed TEXT, translation TEXT NOT NULL, " +
                "updatedAt INTEGER NOT NULL, PRIMARY KEY(surahId, number))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS reciter_downloads (" +
                "reciterId TEXT NOT NULL, surahId INTEGER NOT NULL, " +
                "fileUri TEXT NOT NULL, downloadedAt INTEGER NOT NULL, " +
                "PRIMARY KEY(reciterId, surahId))"
        )
    }
}
