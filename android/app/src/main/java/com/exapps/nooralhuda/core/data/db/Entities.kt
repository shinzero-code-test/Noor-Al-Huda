package com.exapps.nooralhuda.core.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/** Opaque cached responses only. Never the primary store for searchable content. */
@Entity(tableName = "content_cache")
data class ContentCacheEntry(
    @PrimaryKey val key: String,
    val bucket: String,
    val payload: String,
    val updatedAt: Long
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val key: String,
    val surahId: Int,
    val ayahNumber: Int,
    val surahName: String,
    val createdAt: Long
) {
    companion object {
        fun keyOf(surahId: Int, ayahNumber: Int): String = "$surahId:$ayahNumber"
    }
}

@Dao
interface ContentCacheDao {
    @Query("SELECT payload FROM content_cache WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entry: ContentCacheEntry)

    @Query("DELETE FROM content_cache WHERE `key` = :key")
    suspend fun delete(key: String)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    suspend fun all(): List<BookmarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM bookmarks")
    suspend fun clear()
}
