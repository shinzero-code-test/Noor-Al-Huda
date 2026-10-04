package com.exapps.nooralhuda.feature.home

import androidx.lifecycle.ViewModel
import com.exapps.nooralhuda.core.datetime.HijriDateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class HomeUiState(
    val greetingRes: Int,
    val hijriDate: String
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    hijriDates: HijriDateProvider
) : ViewModel() {

    private val hour = Calendar.getInstance().get(Locale.getDefault()).get(Calendar.HOUR_OF_DAY)

    private val _uiState = MutableStateFlow(
        HomeUiState(
            greetingRes = greetingFor(hour),
            hijriDate = hijriDates.today(Locale.getDefault().toLanguageTag())
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    companion object {
        fun greetingFor(hour: Int): Int = when (hour) {
            in 5..11 -> com.exapps.nooralhuda.R.string.home_greeting_morning
            in 12..17 -> com.exapps.nooralhuda.R.string.home_greeting_afternoon
            else -> com.exapps.nooralhuda.R.string.home_greeting_evening
        }
    }
}
