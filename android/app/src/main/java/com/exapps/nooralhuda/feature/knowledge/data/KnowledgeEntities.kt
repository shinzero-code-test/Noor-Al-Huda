package com.exapps.nooralhuda.feature.knowledge.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "knowledge_entries")
data class KnowledgeEntryEntity(
    @PrimaryKey val id: String,
    val kind: String,
    val title: String,
    val subtitle: String,
    val body: String,
    val url: String?
)

@Dao
interface KnowledgeEntryDao {
    @Query("SELECT * FROM knowledge_entries ORDER BY kind, id")
    fun allFlow(): Flow<List<KnowledgeEntryEntity>>

    @Query("SELECT * FROM knowledge_entries ORDER BY kind, id")
    suspend fun all(): List<KnowledgeEntryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entries: List<KnowledgeEntryEntity>)

    @Query("SELECT COUNT(*) FROM knowledge_entries")
    suspend fun count(): Int
}
