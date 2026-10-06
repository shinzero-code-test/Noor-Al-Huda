package com.exapps.nooralhuda.feature.seerah.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "seerah_chapters")
data class SeerahChapterEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val reflection: String,
    /** Lessons joined with U+001F (unit separator). */
    val lessons: String
)

@Dao
interface SeerahChapterDao {
    @Query("SELECT * FROM seerah_chapters ORDER BY id")
    fun allFlow(): Flow<List<SeerahChapterEntity>>

    @Query("SELECT * FROM seerah_chapters ORDER BY id")
    suspend fun all(): List<SeerahChapterEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(chapters: List<SeerahChapterEntity>)

    @Query("SELECT COUNT(*) FROM seerah_chapters")
    suspend fun count(): Int
}
