package com.exapps.nooralhuda.core.datetime

import android.icu.text.SimpleDateFormat
import android.icu.util.IslamicCalendar
import android.icu.util.TimeZone
import android.icu.util.ULocale
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/** Single Islamic-calendar abstraction. UI never touches android.icu directly. */
fun interface HijriDateProvider {
    /** e.g. "14 Ramadan 1447" in the requested locale. */
    fun today(localeTag: String): String
}

@Singleton
class IslamicHijriDateProvider @Inject constructor() : HijriDateProvider {
    override fun today(localeTag: String): String {
        val calendar = IslamicCalendar(TimeZone.getDefault(), ULocale(localeTag))
        calendar.timeInMillis = System.currentTimeMillis()
        val format = SimpleDateFormat("d MMMM yyyy", ULocale(localeTag))
        format.calendar = calendar
        return format.format(Date(calendar.timeInMillis))
    }
}
