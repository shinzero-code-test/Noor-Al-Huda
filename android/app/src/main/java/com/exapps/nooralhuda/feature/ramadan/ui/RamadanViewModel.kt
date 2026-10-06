package com.exapps.nooralhuda.feature.ramadan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exapps.nooralhuda.core.data.prefs.PreferencesStore
import com.exapps.nooralhuda.core.datetime.HijriMonthProvider
import com.exapps.nooralhuda.feature.calendar.domain.CalendarRepository
import com.exapps.nooralhuda.feature.calendar.domain.IslamicEvent
import com.exapps.nooralhuda.feature.prayer.data.PrayerRepository
import com.exapps.nooralhuda.feature.prayer.domain.PrayerName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class RamadanUiState(
    val isRamadan: Boolean = false,
    val monthsToRamadan: Int = 0,
    val suhoorLabel: String? = null,
    val iftarLabel: String? = null,
    val tarawih: Int = 0,
    val money: String = "",
    val gold: String = "",
    val silver: String = "",
    val zakat: String = "0.00",
    val events: List<IslamicEvent> = emptyList()
)

@HiltViewModel
class RamadanViewModel @Inject constructor(
    private val months: HijriMonthProvider,
    private val prayers: PrayerRepository,
    private val prefs: PreferencesStore,
    private val calendar: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RamadanUiState())
    val uiState: StateFlow<RamadanUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val current = months.month(0)
            val isRamadan = current.hijriMonth == 9
            _uiState.value = _uiState.value.copy(
                isRamadan = isRamadan,
                monthsToRamadan = (9 - current.hijriMonth + 12) % 12
            )
            loadMealTimes()
        }
        viewModelScope.launch {
            calendar.warm()
            calendar.events.collect { events ->
                _uiState.value = _uiState.value.copy(events = events)
            }
        }
    }

    /** Suhoor ends at Fajr, iftar at Maghrib — from the stored location only. */
    private suspend fun loadMealTimes() {
        val location = prefs.prayerLocation() ?: return
        val method = prayers.methodOf(prefs.calcMethod() ?: "ummAlQura")
        val day = try {
            prayers.day(location, method, java.util.Date())
        } catch (ignored: Exception) {
            return
        }
        val fajr = day.times.firstOrNull { it.name == PrayerName.FAJR }?.at
        val maghrib = day.times.firstOrNull { it.name == PrayerName.MAGHRIB }?.at
        val fmt = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
        _uiState.value = _uiState.value.copy(
            suhoorLabel = fajr?.let { fmt.format(it) },
            iftarLabel = maghrib?.let { fmt.format(it) }
        )
    }

    fun addTarawih() {
        _uiState.value = _uiState.value.copy(tarawih = _uiState.value.tarawih + 2)
    }

    fun removeTarawih() {
        _uiState.value = _uiState.value.copy(
            tarawih = (_uiState.value.tarawih - 2).coerceAtLeast(0)
        )
    }

    fun onMoney(input: String) = onZakatInput(input, null, null)
    fun onGold(input: String) = onZakatInput(null, input, null)
    fun onSilver(input: String) = onZakatInput(null, null, input)

    private fun onZakatInput(money: String?, gold: String?, silver: String?) {
        val current = _uiState.value
        val next = current.copy(
            money = money ?: current.money,
            gold = gold ?: current.gold,
            silver = silver ?: current.silver
        )
        val total = next.money.toDoubleOrNull().orZero() +
            next.gold.toDoubleOrNull().orZero() +
            next.silver.toDoubleOrNull().orZero()
        _uiState.value = next.copy(zakat = "%.2f".format(total * 0.025))
    }

    private fun Double?.orZero(): Double = this?.takeIf { it.isFinite() } ?: 0.0
}
