package com.exapps.nooralhuda.feature.dua.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "dua_entries")
data class DuaEntryEntity(
    @PrimaryKey val id: String,
    val category: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val repeat: Int,
    val source: String,
    val favourite: Boolean = false
)

@Dao
interface DuaEntryDao {
    @Query("SELECT * FROM dua_entries ORDER BY category, id")
    fun allFlow(): Flow<List<DuaEntryEntity>>

    @Query("SELECT * FROM dua_entries ORDER BY category, id")
    suspend fun all(): List<DuaEntryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entries: List<DuaEntryEntity>)

    @Query("UPDATE dua_entries SET favourite = NOT favourite WHERE id = :id")
    suspend fun toggleFavourite(id: String)

    @Query("SELECT COUNT(*) FROM dua_entries")
    suspend fun count(): Int
}
