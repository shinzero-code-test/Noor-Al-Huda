package com.exapps.nooralhuda.feature.radio.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "radio_stations")
data class RadioStationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val url: String,
    val favourite: Boolean = false
)

@Dao
interface RadioStationDao {
    @Query("SELECT * FROM radio_stations ORDER BY favourite DESC, name")
    fun allFlow(): Flow<List<RadioStationEntity>>

    @Query("SELECT * FROM radio_stations ORDER BY favourite DESC, name")
    suspend fun all(): List<RadioStationEntity>

    /**
     * Directory refresh that preserves favourites: new rows insert as
     * non-favourite, existing rows keep their flag but take new names/urls.
     */
    @Query(
        "INSERT INTO radio_stations (id, name, url, favourite) VALUES (:id, :name, :url, 0) " +
            "ON CONFLICT(id) DO UPDATE SET name = excluded.name, url = excluded.url"
    )
    suspend fun upsertPreserveFavourite(id: String, name: String, url: String)

    @Query("UPDATE radio_stations SET favourite = NOT favourite WHERE id = :id")
    suspend fun toggleFavourite(id: String)

    @Query("SELECT COUNT(*) FROM radio_stations")
    suspend fun count(): Int
}
