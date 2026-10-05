package com.exapps.nooralhuda.feature.quran.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey val id: Int,
    val arabic: String,
    val transliteration: String,
    val english: String,
    val verses: Int,
    val revelation: String,
    val updatedAt: Long
)

@Entity(tableName = "verses", primaryKeys = ["surahId", "number"])
data class VerseEntity(
    val surahId: Int,
    val number: Int,
    val arabic: String,
    val tajweed: String?,
    val translation: String,
    val updatedAt: Long
)

@Entity(tableName = "reciter_downloads", primaryKeys = ["reciterId", "surahId"])
data class ReciterDownloadEntity(
    val reciterId: String,
    val surahId: Int,
    val fileUri: String,
    val downloadedAt: Long
)

@Dao
interface SurahDao {
    @Query("SELECT * FROM surahs ORDER BY id")
    suspend fun all(): List<SurahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(surahs: List<SurahEntity>)

    @Query("SELECT COUNT(*) FROM surahs")
    suspend fun count(): Int
}

@Dao
interface VerseDao {
    @Query("SELECT * FROM verses WHERE surahId = :surahId ORDER BY number")
    suspend fun forSurah(surahId: Int): List<VerseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(verses: List<VerseEntity>)

    @Query("SELECT COUNT(*) FROM verses WHERE surahId = :surahId")
    suspend fun countForSurah(surahId: Int): Int
}

@Dao
interface ReciterDownloadDao {
    @Query("SELECT fileUri FROM reciter_downloads WHERE reciterId = :reciterId AND surahId = :surahId LIMIT 1")
    suspend fun fileUri(reciterId: String, surahId: Int): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(download: ReciterDownloadEntity)
}
