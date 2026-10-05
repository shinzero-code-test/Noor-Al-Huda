package com.exapps.nooralhuda.feature.prayer.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Consecutive-day streak from Hijri prayer days (yyyy-MM-dd lexical order
 * works because ISO dates sort chronologically). Pure — unit-tested.
 */
object Streaks {
    private val iso = DateTimeFormatter.ISO_LOCAL_DATE

    fun current(dates: List<String>, today: LocalDate = LocalDate.now()): Int {
        val set = dates.toSet()
        var cursor = today
        // A streak counts if today or yesterday is present (today may be incomplete).
        if (cursor.toString() !in set) cursor = cursor.minusDays(1)
        var streak = 0
        while (cursor.toString() in set) {
            streak += 1
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    fun isValidIsoDay(day: String): Boolean = try {
        LocalDate.parse(day, iso)
        true
    } catch (_: Exception) {
        false
    }
}
