package com.exapps.nooralhuda.feature.prayer.data

import android.content.Context
import com.exapps.nooralhuda.core.data.prefs.PreferencesStore
import com.exapps.nooralhuda.core.notifications.PrayerAlarmScheduler
import com.exapps.nooralhuda.feature.prayer.domain.CalculationMethod
import com.exapps.nooralhuda.feature.prayer.domain.PrayerName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the daily schedule: computes today + tomorrow and arms every prayer
 * the user enabled. Prayer names for notifications come from string resources
 * via [labelFor] in the UI layer.
 */
@Singleton
class PrayerScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prayers: PrayerRepository,
    private val prefs: PreferencesStore
) {
    suspend fun rescheduleAll(labelFor: (PrayerName) -> String = { it.name }) {
        val location = prefs.prayerLocation() ?: return
        val method = prayers.methodOf(prefs.calcMethod() ?: "ummAlQura")
        val enabled = prefs.enabledPrayers()
        val today = Date()
        val tomorrow = Date(today.time + 86_400_000L)
        for (day in listOf(
            prayers.day(location, method, today),
            prayers.day(location, method, tomorrow)
        )) {
            for (time in day.times) {
                if (time.name == PrayerName.SUNRISE) continue
                if (time.name.name !in enabled) {
                    PrayerAlarmScheduler.cancel(context, time.name)
                    continue
                }
                PrayerAlarmScheduler.schedule(context, time.name, labelFor(time.name), time.at.time)
            }
        }
    }

    companion object {
        val DEFAULT_ENABLED: Set<String> = PrayerName.entries
            .filter { it != PrayerName.SUNRISE }
            .map { it.name }
            .toSet()
    }
}
