package com.exapps.nooralhuda.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.exapps.nooralhuda.feature.prayer.data.AzkarDao
import com.exapps.nooralhuda.feature.prayer.data.AzkarEntity
import com.exapps.nooralhuda.feature.prayer.data.PrayerDayDao
import com.exapps.nooralhuda.feature.prayer.data.PrayerDayEntity
import com.exapps.nooralhuda.feature.prayer.data.WorshipLogDao
import com.exapps.nooralhuda.feature.prayer.data.WorshipLogEntity
import com.exapps.nooralhuda.feature.quran.data.ReciterDownloadDao
import com.exapps.nooralhuda.feature.quran.data.ReciterDownloadEntity
import com.exapps.nooralhuda.feature.quran.data.SurahDao
import com.exapps.nooralhuda.feature.quran.data.SurahEntity
import com.exapps.nooralhuda.feature.hadith.data.HadithCollectionDao
import com.exapps.nooralhuda.feature.hadith.data.HadithCollectionEntity
import com.exapps.nooralhuda.feature.hadith.data.HadithDetailDao
import com.exapps.nooralhuda.feature.hadith.data.HadithDetailEntity
import com.exapps.nooralhuda.feature.hadith.data.HadithItemDao
import com.exapps.nooralhuda.feature.hadith.data.HadithItemEntity
import com.exapps.nooralhuda.feature.hadith.data.HadithRemoteKeyDao
import com.exapps.nooralhuda.feature.hadith.data.HadithRemoteKeyEntity

/** v5 adds the hadith library tables (collections, items, remote keys, details). */
@Database(
    entities = [
        ContentCacheEntry::class,
        BookmarkEntity::class,
        SurahEntity::class,
        VerseEntity::class,
        ReciterDownloadEntity::class,
        PrayerDayEntity::class,
        WorshipLogEntity::class,
        AzkarEntity::class,
        HadithCollectionEntity::class,
        HadithItemEntity::class,
        HadithRemoteKeyEntity::class,
        HadithDetailEntity::class
    ],
    version = 5
)
abstract class NoorDatabase : RoomDatabase() {
    abstract fun contentCacheDao(): ContentCacheDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun surahDao(): SurahDao
    abstract fun verseDao(): VerseDao
    abstract fun reciterDownloadDao(): ReciterDownloadDao
    abstract fun prayerDayDao(): PrayerDayDao
    abstract fun worshipLogDao(): WorshipLogDao
    abstract fun azkarDao(): AzkarDao
    abstract fun hadithCollectionDao(): HadithCollectionDao
    abstract fun hadithItemDao(): HadithItemDao
    abstract fun hadithRemoteKeyDao(): HadithRemoteKeyDao
    abstract fun hadithDetailDao(): HadithDetailDao
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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS prayer_days (" +
                "`key` TEXT NOT NULL PRIMARY KEY, date TEXT NOT NULL, " +
                "fajr INTEGER NOT NULL, sunrise INTEGER NOT NULL, dhuhr INTEGER NOT NULL, " +
                "asr INTEGER NOT NULL, maghrib INTEGER NOT NULL, isha INTEGER NOT NULL, " +
                "qibla REAL NOT NULL, locationLabel TEXT NOT NULL, method TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS worship_log (" +
                "hijri TEXT NOT NULL, activity TEXT NOT NULL, value INTEGER NOT NULL, " +
                "PRIMARY KEY(hijri, activity))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS azkar_entries (" +
                "collection TEXT NOT NULL, entryId TEXT NOT NULL, text TEXT NOT NULL, " +
                "`count` INTEGER NOT NULL, virtue TEXT NOT NULL, updatedAt INTEGER NOT NULL, " +
                "PRIMARY KEY(collection, entryId))"
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS hadith_collections (" +
                "id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, " +
                "count INTEGER NOT NULL, `group` TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS hadith_items (" +
                "collectionId TEXT NOT NULL, id TEXT NOT NULL, title TEXT NOT NULL, " +
                "sortKey INTEGER NOT NULL, PRIMARY KEY(collectionId, id))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS hadith_remote_keys (" +
                "collectionId TEXT NOT NULL PRIMARY KEY, nextPage INTEGER)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS hadith_details (" +
                "id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, arabic TEXT NOT NULL, " +
                "english TEXT, source TEXT NOT NULL, updatedAt INTEGER NOT NULL)"
        )
    }
}
