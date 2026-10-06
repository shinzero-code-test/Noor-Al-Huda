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
import com.exapps.nooralhuda.feature.quran.data.VerseDao
import com.exapps.nooralhuda.feature.quran.data.VerseEntity
import com.exapps.nooralhuda.feature.hadith.data.HadithCollectionDao
import com.exapps.nooralhuda.feature.hadith.data.HadithCollectionEntity
import com.exapps.nooralhuda.feature.hadith.data.HadithDetailDao
import com.exapps.nooralhuda.feature.hadith.data.HadithDetailEntity
import com.exapps.nooralhuda.feature.hadith.data.HadithItemDao
import com.exapps.nooralhuda.feature.hadith.data.HadithItemEntity
import com.exapps.nooralhuda.feature.radio.data.RadioStationDao
import com.exapps.nooralhuda.feature.radio.data.RadioStationEntity
import com.exapps.nooralhuda.feature.calendar.data.CalendarEventDao
import com.exapps.nooralhuda.feature.calendar.data.CalendarEventEntity
import com.exapps.nooralhuda.feature.dua.data.DuaEntryDao
import com.exapps.nooralhuda.feature.dua.data.DuaEntryEntity
import com.exapps.nooralhuda.feature.knowledge.data.KnowledgeEntryDao
import com.exapps.nooralhuda.feature.knowledge.data.KnowledgeEntryEntity
import com.exapps.nooralhuda.feature.seerah.data.SeerahChapterDao
import com.exapps.nooralhuda.feature.seerah.data.SeerahChapterEntity

/** v7 adds the 1.2.0 content tables (dua, calendar, seerah, knowledge). */
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
        HadithDetailEntity::class,
        RadioStationEntity::class,
        DuaEntryEntity::class,
        CalendarEventEntity::class,
        SeerahChapterEntity::class,
        KnowledgeEntryEntity::class
    ],
    version = 7
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
    abstract fun hadithDetailDao(): HadithDetailDao
    abstract fun radioStationDao(): RadioStationDao
    abstract fun duaEntryDao(): DuaEntryDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun seerahChapterDao(): SeerahChapterDao
    abstract fun knowledgeEntryDao(): KnowledgeEntryDao
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
            "CREATE TABLE IF NOT EXISTS hadith_details (" +
                "id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, arabic TEXT NOT NULL, " +
                "english TEXT, source TEXT NOT NULL, updatedAt INTEGER NOT NULL)"
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS radio_stations (" +
                "id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, " +
                "url TEXT NOT NULL, favourite INTEGER NOT NULL DEFAULT 0)"
        )
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS dua_entries (" +
                "id TEXT NOT NULL PRIMARY KEY, category TEXT NOT NULL, arabic TEXT NOT NULL, " +
                "transliteration TEXT NOT NULL, translation TEXT NOT NULL, repeat INTEGER NOT NULL, " +
                "source TEXT NOT NULL, favourite INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS calendar_events (" +
                "id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, hijriMonth INTEGER NOT NULL, " +
                "hijriDay INTEGER NOT NULL, description TEXT NOT NULL, " +
                "reminder INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS seerah_chapters (" +
                "id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, summary TEXT NOT NULL, " +
                "reflection TEXT NOT NULL, lessons TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS knowledge_entries (" +
                "id TEXT NOT NULL PRIMARY KEY, kind TEXT NOT NULL, title TEXT NOT NULL, " +
                "subtitle TEXT NOT NULL, body TEXT NOT NULL, url TEXT)"
        )
    }
}
