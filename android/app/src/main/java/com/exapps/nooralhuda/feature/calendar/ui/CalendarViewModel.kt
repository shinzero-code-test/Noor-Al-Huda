package com.exapps.nooralhuda.feature.calendar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exapps.nooralhuda.core.datetime.HijriMonth
import com.exapps.nooralhuda.core.datetime.HijriMonthProvider
import com.exapps.nooralhuda.feature.calendar.domain.CalendarRepository
import com.exapps.nooralhuda.feature.calendar.domain.IslamicEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CalendarUiState(
    val month: HijriMonth? = null,
    val offset: Int = 0,
    val events: List<IslamicEvent> = emptyList(),
    val today: LocalDate = LocalDate.now(),
    val localeAr: Boolean = false
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repo: CalendarRepository,
    private val months: HijriMonthProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CalendarUiState(localeAr = java.util.Locale.getDefault().language == "ar")
    )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.warm()
            repo.events.collect { events ->
                _uiState.value = _uiState.value.copy(
                    events = events,
                    month = months.month(_uiState.value.offset)
                )
            }
        }
    }

    fun shiftMonth(delta: Int) {
        val offset = (_uiState.value.offset + delta).coerceIn(-120, 120)
        _uiState.value = _uiState.value.copy(
            offset = offset,
            month = months.month(offset)
        )
    }

    fun toggleReminder(event: IslamicEvent) {
        viewModelScope.launch { repo.toggleReminder(event.id) }
    }

    fun monthTitle(): String {
        val month = _uiState.value.month ?: return ""
        val tag = if (_uiState.value.localeAr) "ar" else "en"
        val name = months.monthName(month.hijriMonth, tag)
        return if (_uiState.value.localeAr) "$name ${month.hijriYear}" else "$name ${month.hijriYear} AH"
    }
}
