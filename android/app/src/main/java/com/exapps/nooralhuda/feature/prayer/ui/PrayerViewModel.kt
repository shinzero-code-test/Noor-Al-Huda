package com.exapps.nooralhuda.feature.prayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exapps.nooralhuda.core.data.prefs.PreferencesStore
import com.exapps.nooralhuda.core.datetime.HijriDateProvider
import com.exapps.nooralhuda.core.location.LocationRepository
import com.exapps.nooralhuda.feature.prayer.data.DeviceLocation
import com.exapps.nooralhuda.feature.prayer.data.PrayerRepository
import com.exapps.nooralhuda.feature.prayer.data.PrayerScheduler
import com.exapps.nooralhuda.feature.prayer.domain.PrayerDay
import com.exapps.nooralhuda.feature.prayer.domain.PrayerName
import com.exapps.nooralhuda.feature.prayer.domain.Streaks
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class PrayerUiState(
    val loading: Boolean = true,
    val day: PrayerDay? = null,
    val hijriDate: String = "",
    val prayedToday: Set<String> = emptySet(),
    val streakDays: Int = 0,
    val enabledAlarms: Set<String> = PrayerScheduler.DEFAULT_ENABLED,
    val exactAlarmAllowed: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val prayers: PrayerRepository,
    private val location: LocationRepository,
    private val prefs: PreferencesStore,
    private val worship: com.exapps.nooralhuda.feature.prayer.data.WorshipLogDao,
    private val hijri: HijriDateProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            try {
                val stored = prefs.prayerLocation()
                val current = stored ?: run {
                    val fix = location.fresh() ?: location.lastKnown()
                    fix?.let {
                        val label = "%.3f, %.3f".format(it.lat, it.lng)
                        prefs.setPrayerLocation(it.lat, it.lng, label)
                        DeviceLocation(it.lat, it.lng, label)
                    }
                } ?: throw IllegalStateException("Location unavailable")
                val method = prayers.methodOf(prefs.calcMethod() ?: "ummAlQura")
                val day = prayers.day(current, method)
                val hijriToday = hijri.today(Locale.getDefault().toLanguageTag())
                val log = worship.forDate(hijriToday)
                val prayed = log.filter { it.activity.startsWith("prayer:") && it.value > 0 }
                    .map { it.activity.removePrefix("prayer:") }
                    .toSet()
                val days = worship.prayerDays()
                _uiState.value = PrayerUiState(
                    loading = false,
                    day = day,
                    hijriDate = hijriToday,
                    prayedToday = prayed,
                    streakDays = Streaks.current(days),
                    enabledAlarms = prefs.enabledPrayers().ifEmpty { PrayerScheduler.DEFAULT_ENABLED }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, error = e.message)
            }
        }
    }

    fun togglePrayed(prayer: PrayerName, hijriDate: String) {
        viewModelScope.launch {
            val key = "prayer:${prayer.name}"
            val existing = worship.forDate(hijriDate).firstOrNull { it.activity == key }
            val next = if ((existing?.value ?: 0) > 0) 0 else 1
            worship.upsert(
                com.exapps.nooralhuda.feature.prayer.data.WorshipLogEntity(hijriDate, key, next)
            )
            load()
        }
    }

    fun setAlarmEnabled(prayer: PrayerName, enabled: Boolean) {
        viewModelScope.launch {
            val current = prefs.enabledPrayers().ifEmpty { PrayerScheduler.DEFAULT_ENABLED }.toMutableSet()
            if (enabled) current.add(prayer.name) else current.remove(prayer.name)
            prefs.setEnabledPrayers(current)
            _uiState.value = _uiState.value.copy(enabledAlarms = current)
        }
    }

    fun exactAlarmAllowed(allowed: Boolean) {
        _uiState.value = _uiState.value.copy(exactAlarmAllowed = allowed)
    }
}
