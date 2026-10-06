package com.exapps.nooralhuda.feature.hadith.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "hadith_collections")
data class HadithCollectionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val count: Int,
    val group: String
)

@Entity(
    tableName = "hadith_items",
    primaryKeys = ["collectionId", "id"]
)
data class HadithItemEntity(
    val collectionId: String,
    val id: String,
    val title: String,
    /** page * 1000 + index: stable paging order across refreshes. */
    val sortKey: Int
)

@Entity(tableName = "hadith_details")
data class HadithDetailEntity(
    @PrimaryKey val id: String,
    val title: String,
    val arabic: String,
    val english: String?,
    val source: String,
    val updatedAt: Long
)

@Dao
interface HadithCollectionDao {
    @Query("SELECT * FROM hadith_collections ORDER BY id")
    suspend fun all(): List<HadithCollectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(collections: List<HadithCollectionEntity>)

    @Query("SELECT COUNT(*) FROM hadith_collections")
    suspend fun count(): Int
}

@Dao
interface HadithItemDao {
    @Query(
        "SELECT * FROM hadith_items WHERE collectionId = :collectionId " +
            "ORDER BY sortKey LIMIT :limit OFFSET :offset"
    )
    suspend fun pageByCollection(
        collectionId: String,
        limit: Int,
        offset: Int
    ): List<HadithItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<HadithItemEntity>)

    @Query("DELETE FROM hadith_items WHERE collectionId = :collectionId")
    suspend fun clearByCollection(collectionId: String)

    @Query("SELECT COUNT(*) FROM hadith_items WHERE collectionId = :collectionId")
    suspend fun countByCollection(collectionId: String): Int
}

@Dao
interface HadithDetailDao {
    @Query("SELECT * FROM hadith_details WHERE id = :id LIMIT 1")
    suspend fun get(id: String): HadithDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(detail: HadithDetailEntity)
}
