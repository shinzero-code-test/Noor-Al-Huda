package com.exapps.nooralhuda.core.datetime

import android.icu.util.IslamicCalendar
import android.icu.util.TimeZone
import android.icu.util.ULocale
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/** One cell in the month grid. */
data class HijriDay(
    val hijriDay: Int,
    val gregorian: LocalDate
)

/** A Hijri month with its Gregorian-mapped cells. */
data class HijriMonth(
    val hijriYear: Int,
    /** 1-based (1 = Muharram). */
    val hijriMonth: Int,
    val days: List<HijriDay>
)

interface HijriMonthProvider {
    /** Month grid for the current month plus [offset] months. */
    fun month(offset: Int = 0): HijriMonth

    /** Localised month name, e.g. "Rabi al-Awwal" / "ربيع الأول". */
    fun monthName(month: Int, localeTag: String): String

    fun todayGregorian(): LocalDate = LocalDate.now()
}

@Singleton
class IslamicHijriMonthProvider @Inject constructor() : HijriMonthProvider {
    override fun month(offset: Int): HijriMonth {
        val cal = IslamicCalendar(TimeZone.getDefault(), ULocale.ENGLISH)
        cal.timeInMillis = System.currentTimeMillis()
        cal.add(IslamicCalendar.MONTH, offset)
        val year = cal.get(IslamicCalendar.YEAR)
        // IslamicCalendar.MONTH is 0-based (Muharram = 0).
        val month = cal.get(IslamicCalendar.MONTH) + 1
        val length = cal.getActualMaximum(IslamicCalendar.DAY_OF_MONTH)
        val zone = ZoneId.systemDefault()
        val days = (1..length).map { day ->
            cal.set(IslamicCalendar.DAY_OF_MONTH, day)
            HijriDay(
                hijriDay = day,
                gregorian = java.time.Instant.ofEpochMilli(cal.timeInMillis)
                    .atZone(zone).toLocalDate()
            )
        }
        return HijriMonth(year, month, days)
    }

    override fun monthName(month: Int, localeTag: String): String {
        val namesEn = listOf(
            "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani",
            "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Shaban",
            "Ramadan", "Shawwal", "Dhu al-Qadah", "Dhu al-Hijjah"
        )
        val namesAr = listOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )
        val table = if (localeTag.startsWith("ar")) namesAr else namesEn
        return table.getOrElse(month - 1) { "" }
    }
}

/** Monday-first offset for a month grid (0 = starts Monday). */
fun DayOfWeek.mondayFirstIndex(): Int = (value - 1) % 7
