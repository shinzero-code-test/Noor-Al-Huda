package com.exapps.nooralhuda.feature.prayer.domain

import java.util.Calendar
import java.util.TimeZone

/**
 * Consecutive-day streak from Hijri prayer days. ISO dates (yyyy-MM-dd)
 * sort chronologically as plain strings; day arithmetic uses Calendar so
 * this works on minSdk 24 with no desugaring. Pure — unit-tested.
 */
object Streaks {

    fun current(dates: List<String>, today: String = todayIso()): Int {
        if (!isValidIsoDay(today)) return 0
        val set = dates.toSet()
        var cursor = if (today in set) today else prevDay(today)
        var streak = 0
        while (cursor != null && cursor in set) {
            streak += 1
            cursor = prevDay(cursor)
        }
        return streak
    }

    fun isValidIsoDay(day: String): Boolean {
        val parts = day.split("-")
        if (parts.size != 3) return false
        val (y, m, d) = parts.map { it.toIntOrNull() ?: return false }
        if (m !in 1..12 || d !in 1..31) return false
        return try {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                isLenient = false
                set(y, m - 1, d, 12, 0, 0)
            }
            cal.timeInMillis
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun todayIso(): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        return isoOf(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun prevDay(day: String): String? {
        val parts = day.split("-")
        if (parts.size != 3) return null
        val y = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val d = parts[2].toIntOrNull() ?: return null
        return try {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                isLenient = false
                set(y, m - 1, d, 12, 0, 0)
                add(Calendar.DAY_OF_MONTH, -1)
            }
            isoOf(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun isoOf(y: Int, m: Int, d: Int): String =
        "%04d-%02d-%02d".format(y, m, d)
}
