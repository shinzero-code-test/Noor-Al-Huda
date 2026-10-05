package com.exapps.nooralhuda.feature.prayer.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/** One cached day per rounded location + method. */
@Entity(tableName = "prayer_days")
data class PrayerDayEntity(
    @PrimaryKey val key: String,
    val date: String,
    val fajr: Long,
    val sunrise: Long,
    val dhuhr: Long,
    val asr: Long,
    val maghrib: Long,
    val isha: Long,
    val qibla: Double,
    val locationLabel: String,
    val method: String
)

/** Worship log: one row per (hijri date, activity). Prayer rows use "prayer:fajr"… */
@Entity(tableName = "worship_log", primaryKeys = ["hijri", "activity"])
data class WorshipLogEntity(
    val hijri: String,
    val activity: String,
    val value: Int
)

@Entity(tableName = "azkar_entries", primaryKeys = ["collection", "entryId"])
data class AzkarEntity(
    val collection: String,
    val entryId: String,
    val text: String,
    val count: Int,
    val virtue: String,
    val updatedAt: Long
)

@Dao
interface PrayerDayDao {
    @Query("SELECT * FROM prayer_days WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): PrayerDayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: PrayerDayEntity)
}

@Dao
interface WorshipLogDao {
    @Query("SELECT * FROM worship_log WHERE hijri = :hijri")
    suspend fun forDate(hijri: String): List<WorshipLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WorshipLogEntity)

    @Query("SELECT hijri FROM worship_log WHERE activity LIKE 'prayer:%' AND value > 0 GROUP BY hijri ORDER BY hijri DESC")
    suspend fun prayerDays(): List<String>
}

@Dao
interface AzkarDao {
    @Query("SELECT * FROM azkar_entries WHERE collection = :collection ORDER BY entryId")
    suspend fun forCollection(collection: String): List<AzkarEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<AzkarEntity>)
}
