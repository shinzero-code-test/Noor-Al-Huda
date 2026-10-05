package com.exapps.nooralhuda.feature.prayer.domain

import java.util.Date

enum class PrayerName { FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA }

enum class CalculationMethod { UMM_AL_QURA, EGYPTIAN, KARACHI }

data class PrayerTime(val name: PrayerName, val at: Date)

data class PrayerDay(
    val dateKey: String,
    val times: List<PrayerTime>,
    val qiblaDegrees: Double,
    val locationLabel: String,
    val method: CalculationMethod
) {
    fun next(now: Long = System.currentTimeMillis()): PrayerTime? =
        times.firstOrNull { it.at.time > now }
}
