package com.exapps.nooralhuda.feature.azkar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exapps.nooralhuda.core.tts.NoorTts
import com.exapps.nooralhuda.feature.prayer.data.AzkarEntry
import com.exapps.nooralhuda.feature.prayer.data.AzkarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AzkarUiState(
    val loading: Boolean = true,
    val collection: String = "morning",
    val query: String = "",
    val entries: List<AzkarEntry> = emptyList(),
    val progress: Map<String, Int> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class AzkarViewModel @Inject constructor(
    private val repo: AzkarRepository,
    private val tts: NoorTts
) : ViewModel() {

    private val _uiState = MutableStateFlow(AzkarUiState())
    val uiState: StateFlow<AzkarUiState> = _uiState.asStateFlow()

    init {
        load("morning")
    }

    fun load(collection: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, collection = collection, error = null)
            try {
                val entries = repo.collection(collection)
                _uiState.value = _uiState.value.copy(loading = false, entries = entries)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, error = e.message)
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun increment(entry: AzkarEntry) {
        val done = _uiState.value.progress[entry.id] ?: 0
        if (done >= entry.count) return
        _uiState.value = _uiState.value.copy(
            progress = _uiState.value.progress + (entry.id to done + 1)
        )
    }

    fun reset(entry: AzkarEntry) {
        _uiState.value = _uiState.value.copy(
            progress = _uiState.value.progress - entry.id
        )
    }

    fun speak(entry: AzkarEntry) {
        tts.speak(entry.text)
    }

    override fun onCleared() {
        tts.stop()
    }
}
