package com.exapps.nooralhuda.feature.prayer.data

import com.batoulapps.adhan.CalculationMethod as AdhanMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.Qibla
import com.batoulapps.adhan.data.DateComponents
import com.exapps.nooralhuda.feature.prayer.domain.CalculationMethod
import com.exapps.nooralhuda.feature.prayer.domain.PrayerDay
import com.exapps.nooralhuda.feature.prayer.domain.PrayerName
import com.exapps.nooralhuda.feature.prayer.domain.PrayerTime
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

data class DeviceLocation(val lat: Double, val lng: Double, val label: String)

/**
 * Local astronomical calculation (adhan lib) — fully offline.
 * Room caches one row per rounded location + method + date.
 */
@Singleton
class PrayerRepository @Inject constructor(
    private val dao: PrayerDayDao
) {
    fun methodOf(name: String): CalculationMethod = when (name) {
        "egyptian" -> CalculationMethod.EGYPTIAN
        "karachi" -> CalculationMethod.KARACHI
        else -> CalculationMethod.UMM_AL_QURA
    }

    suspend fun day(
        location: DeviceLocation,
        method: CalculationMethod,
        date: Date = Date()
    ): PrayerDay {
        val key = "${location.lat.toString().take(7)}:${location.lng.toString().take(7)}:" +
            "${dateKey(date)}:${method.name}"
        dao.get(key)?.let { return it.toDomain() }
        val computed = compute(location, method, date)
        dao.upsert(computed.toEntity(key, location, method, date))
        return computed
    }

    fun compute(location: DeviceLocation, method: CalculationMethod, date: Date): PrayerDay {
        val coordinates = Coordinates(location.lat, location.lng)
        val params = when (method) {
            CalculationMethod.UMM_AL_QURA -> AdhanMethod.UMM_AL_QURA_CALCULATION_METHOD.parameters
            CalculationMethod.EGYPTIAN -> AdhanMethod.EGYPTIAN_GENERAL_AUTHORITY.parameters
            CalculationMethod.KARACHI -> AdhanMethod.KARACHI.parameters
        }
        val cal = Calendar.getInstance(TimeZone.getDefault()).apply { time = date }
        val components = DateComponents(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        val times = PrayerTimes(coordinates, components, params)
        val list = listOf(
            PrayerName.FAJR to times.fajr,
            PrayerName.SUNRISE to times.sunrise,
            PrayerName.DHUHR to times.dhuhr,
            PrayerName.ASR to times.asr,
            PrayerName.MAGHRIB to times.maghrib,
            PrayerName.ISHA to times.isha
        ).map { (name, d) -> PrayerTime(name, Date(d.time)) }
        return PrayerDay(
            dateKey = dateKey(date),
            times = list,
            qiblaDegrees = Qibla.qibla(coordinates),
            locationLabel = location.label,
            method = method
        )
    }

    private fun dateKey(date: Date): String {
        val cal = Calendar.getInstance().apply { time = date }
        return "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun PrayerDayEntity.toDomain(): PrayerDay {
        val base = this
        return PrayerDay(
            dateKey = date,
            times = listOf(
                PrayerName.FAJR to Date(fajr),
                PrayerName.SUNRISE to Date(sunrise),
                PrayerName.DHUHR to Date(dhuhr),
                PrayerName.ASR to Date(asr),
                PrayerName.MAGHRIB to Date(maghrib),
                PrayerName.ISHA to Date(isha)
            ).map { (name, at) -> PrayerTime(name, at) },
            qiblaDegrees = base.qibla,
            locationLabel = locationLabel,
            method = CalculationMethod.valueOf(method)
        )
    }

    private fun PrayerDay.toEntity(
        key: String,
        location: DeviceLocation,
        method: CalculationMethod,
        date: Date
    ): PrayerDayEntity {
        fun at(name: PrayerName): Long = times.first { it.name == name }.at.time
        return PrayerDayEntity(
            key = key,
            date = dateKey(date),
            fajr = at(PrayerName.FAJR),
            sunrise = at(PrayerName.SUNRISE),
            dhuhr = at(PrayerName.DHUHR),
            asr = at(PrayerName.ASR),
            maghrib = at(PrayerName.MAGHRIB),
            isha = at(PrayerName.ISHA),
            qibla = qiblaDegrees,
            locationLabel = location.label,
            method = method.name
        )
    }
}
