package com.exapps.nooralhuda.feature.quran.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exapps.nooralhuda.core.data.prefs.PendingEmailStore
import com.exapps.nooralhuda.feature.quran.domain.QuranRepository
import com.exapps.nooralhuda.feature.quran.domain.Surah
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuranListUiState(
    val loading: Boolean = true,
    val query: String = "",
    val surahs: List<Surah> = emptyList(),
    val lastReadSurahId: Int? = null,
    val error: String? = null
)

@HiltViewModel
class QuranListViewModel @Inject constructor(
    private val repo: QuranRepository,
    private val prefs: PendingEmailStore
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val lastRead = MutableStateFlow<Int?>(null)
    private val loading = MutableStateFlow(true)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<QuranListUiState> = combine(
        repo.surahs, query, lastRead, loading, error
    ) { surahs, q, last, isLoading, err ->
        val filtered = if (q.isBlank()) surahs
        else surahs.filter {
            it.arabic.contains(q) ||
                it.transliteration.contains(q, ignoreCase = true) ||
                it.id.toString() == q.trim()
        }
        QuranListUiState(isLoading, q, filtered, last, err)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuranListUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            repo.refreshSurahs()
                .onSuccess { loading.value = false }
                .onFailure {
                    loading.value = false
                    error.value = it.message
                }
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun setLastRead(surahId: Int) {
        lastRead.value = surahId
        viewModelScope.launch { prefs.setLastReadSurah(surahId) }
    }
}
