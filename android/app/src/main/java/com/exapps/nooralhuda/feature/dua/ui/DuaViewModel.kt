package com.exapps.nooralhuda.feature.dua.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exapps.nooralhuda.core.tts.NoorTts
import com.exapps.nooralhuda.feature.dua.domain.DuaCategory
import com.exapps.nooralhuda.feature.dua.domain.DuaEntry
import com.exapps.nooralhuda.feature.dua.domain.DuaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DuaUiState(
    val duas: List<DuaEntry> = emptyList(),
    val category: DuaCategory? = null,
    val query: String = "",
    /** Session counters per dua id (repeat progress, not persisted). */
    val counts: Map<String, Int> = emptyMap()
)

@HiltViewModel
class DuaViewModel @Inject constructor(
    private val repo: DuaRepository,
    private val tts: NoorTts
) : ViewModel() {

    private val _uiState = MutableStateFlow(DuaUiState())
    val uiState: StateFlow<DuaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.warm()
            repo.duas.collect { duas ->
                _uiState.value = _uiState.value.copy(duas = duas)
            }
        }
    }

    fun selectCategory(category: DuaCategory?) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun increment(id: String, target: Int) {
        val current = _uiState.value.counts[id] ?: 0
        if (current < target) {
            _uiState.value = _uiState.value.copy(
                counts = _uiState.value.counts + (id to (current + 1))
            )
        }
    }

    fun reset(id: String) {
        _uiState.value = _uiState.value.copy(
            counts = _uiState.value.counts - id
        )
    }

    fun speak(entry: DuaEntry) = tts.speak(entry.arabic)

    fun toggleFavourite(entry: DuaEntry) {
        viewModelScope.launch { repo.toggleFavourite(entry.id) }
    }

    override fun onCleared() {
        tts.stop()
        super.onCleared()
    }
}
