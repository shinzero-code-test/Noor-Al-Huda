package com.exapps.nooralhuda.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exapps.nooralhuda.core.datetime.HijriDateProvider
import com.exapps.nooralhuda.feature.auth.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val greetingRes: Int,
    val hijriDate: String
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    hijriDates: HijriDateProvider,
    auth: AuthRepository
) : ViewModel() {

    private val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    private val _uiState = MutableStateFlow(
        HomeUiState(
            greetingRes = greetingFor(hour),
            hijriDate = hijriDates.today(java.util.Locale.getDefault().toLanguageTag())
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val signedIn: StateFlow<Boolean> = auth.authState
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), auth.authState.value != null)

    companion object {
        fun greetingFor(hour: Int): Int = when (hour) {
            in 5..11 -> com.exapps.nooralhuda.R.string.home_greeting_morning
            in 12..17 -> com.exapps.nooralhuda.R.string.home_greeting_afternoon
            else -> com.exapps.nooralhuda.R.string.home_greeting_evening
        }
    }
}
