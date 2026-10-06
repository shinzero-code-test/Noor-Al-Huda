package com.exapps.nooralhuda.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exapps.nooralhuda.core.data.prefs.PendingEmailStore
import com.exapps.nooralhuda.core.datetime.HijriDateProvider
import com.exapps.nooralhuda.feature.auth.domain.AuthRepository
import com.exapps.nooralhuda.feature.daily.domain.DailyContent
import com.exapps.nooralhuda.feature.daily.domain.DailyContentRepository
import com.exapps.nooralhuda.feature.prayer.data.WorshipLogDao
import com.exapps.nooralhuda.feature.prayer.data.WorshipLogEntity
import com.exapps.nooralhuda.feature.quran.domain.QuranRepository
import com.exapps.nooralhuda.feature.quran.domain.Surah
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val greetingRes: Int,
    val hijriDate: String,
    val resumeSurah: Surah? = null,
    val daily: DailyContent? = null,
    val tasbihCount: Int = 0,
    val tasbihToday: Int = 0,
    val tasbihTarget: Int = 33
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    hijriDates: HijriDateProvider,
    auth: AuthRepository,
    private val quran: QuranRepository,
    private val prefs: PendingEmailStore,
    private val daily: DailyContentRepository,
    private val worship: WorshipLogDao,
    private val hijri: HijriDateProvider
) : ViewModel() {

    private val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    private val base = MutableStateFlow(
        HomeUiState(
            greetingRes = greetingFor(hour),
            hijriDate = hijriDates.today(java.util.Locale.getDefault().toLanguageTag())
        )
    )

    val uiState: StateFlow<HomeUiState> = base.asStateFlow()

    val signedIn: StateFlow<Boolean> = auth.authState
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), auth.authState.value != null)

    init {
        viewModelScope.launch {
            quran.refreshSurahs()
        }
        viewModelScope.launch { refreshDaily() }
        viewModelScope.launch { loadTasbihToday() }
        viewModelScope.launch {
            quran.surahs.collect { surahs ->
                val lastId = prefs.lastReadSurahId() ?: return@collect
                base.value = base.value.copy(
                    resumeSurah = surahs.firstOrNull { it.id == lastId }
                )
            }
        }
    }

    fun setLastRead(surahId: Int) {
        viewModelScope.launch { prefs.setLastReadSurah(surahId) }
    }

    fun refreshDaily() {
        viewModelScope.launch {
            daily.refresh()
            daily.content.collect { content ->
                base.value = base.value.copy(daily = content)
            }
        }
    }

    fun tasbihTap() {
        viewModelScope.launch {
            val next = base.value.tasbihCount + 1
            base.value = base.value.copy(tasbihCount = next)
            val hijriToday = hijri.today(java.util.Locale.getDefault().toLanguageTag())
            val existing = worship.forDate(hijriToday).firstOrNull { it.activity == TASBIH_KEY }
            worship.upsert(WorshipLogEntity(hijriToday, TASBIH_KEY, (existing?.value ?: 0) + 1))
            base.value = base.value.copy(tasbihToday = base.value.tasbihToday + 1)
        }
    }

    fun tasbihReset() {
        base.value = base.value.copy(tasbihCount = 0)
    }

    fun loadTasbihToday() {
        viewModelScope.launch {
            val hijriToday = hijri.today(java.util.Locale.getDefault().toLanguageTag())
            val total = worship.forDate(hijriToday)
                .firstOrNull { it.activity == TASBIH_KEY }?.value ?: 0
            base.value = base.value.copy(tasbihToday = total)
        }
    }

    companion object {
        const val TASBIH_KEY = "tasbih"

        fun greetingFor(hour: Int): Int = when (hour) {
            in 5..11 -> com.exapps.nooralhuda.R.string.home_greeting_morning
            in 12..17 -> com.exapps.nooralhuda.R.string.home_greeting_afternoon
            else -> com.exapps.nooralhuda.R.string.home_greeting_evening
        }
    }
}
