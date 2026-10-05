package com.exapps.nooralhuda.feature.prayer.data

import com.exapps.nooralhuda.feature.prayer.domain.CalculationMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Sanity checks on the real adhan calculation (Makkah): ordering, day
 * placement, and a known Qibla bearing from Cairo (~136°).
 */
class PrayerCalcTest {

    private val repo = PrayerRepository(FakePrayerDayDao())

    @Test
    fun `makkah times ordered fajr before isha`() {
        val loc = DeviceLocation(21.4225, 39.8262, "Makkah")
        val day = repo.compute(loc, CalculationMethod.UMM_AL_QURA, Date())
        val times = day.times.map { it.at.time }
        assertEquals(times.sorted(), times)
        assertEquals(6, times.size)
    }

    @Test
    fun `qibla from cairo points southeast`() {
        val loc = DeviceLocation(30.0444, 31.2357, "Cairo")
        val day = repo.compute(loc, CalculationMethod.EGYPTIAN, Date())
        assertTrue(day.qiblaDegrees in 130.0..142.0)
    }

    @Test
    fun `next prayer is in the future`() {
        val loc = DeviceLocation(21.4225, 39.8262, "Makkah")
        // Noon UTC on a fixed date: Dhuhr or Asr must be next.
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.OCTOBER, 5, 12, 0, 0)
        }
        val day = repo.compute(loc, CalculationMethod.UMM_AL_QURA, cal.time)
        val next = day.next(cal.timeInMillis)
        assertTrue(next != null)
        assertTrue(next!!.at.time > cal.timeInMillis)
    }

    private class FakePrayerDayDao : PrayerDayDao {
        private val rows = mutableMapOf<String, PrayerDayEntity>()
        override suspend fun get(key: String) = rows[key]
        override suspend fun upsert(day: PrayerDayEntity) {
            rows[day.key] = day
        }
    }
}
