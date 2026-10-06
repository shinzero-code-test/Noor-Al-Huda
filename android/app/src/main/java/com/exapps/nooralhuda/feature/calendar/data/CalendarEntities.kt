package com.exapps.nooralhuda.feature.calendar.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val hijriMonth: Int,
    val hijriDay: Int,
    val description: String,
    val reminder: Boolean = false
)

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events ORDER BY hijriMonth, hijriDay")
    fun allFlow(): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events ORDER BY hijriMonth, hijriDay")
    suspend fun all(): List<CalendarEventEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(events: List<CalendarEventEntity>)

    @Query("UPDATE calendar_events SET reminder = NOT reminder WHERE id = :id")
    suspend fun toggleReminder(id: String)

    @Query("SELECT COUNT(*) FROM calendar_events")
    suspend fun count(): Int
}
