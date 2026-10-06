package com.exapps.nooralhuda.feature.knowledge.ui

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exapps.nooralhuda.core.tts.NoorTts
import com.exapps.nooralhuda.feature.knowledge.domain.KnowledgeEntry
import com.exapps.nooralhuda.feature.knowledge.domain.KnowledgeKind
import com.exapps.nooralhuda.feature.knowledge.domain.KnowledgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KnowledgeUiState(
    val entries: List<KnowledgeEntry> = emptyList(),
    val query: String = "",
    val section: KnowledgeKind? = null,
    val expandedFaq: String? = null,
    val nameOfTheDay: KnowledgeEntry? = null
)

@HiltViewModel
class KnowledgeViewModel @Inject constructor(
    private val repo: KnowledgeRepository,
    private val tts: NoorTts
) : ViewModel() {

    private val _uiState = MutableStateFlow(KnowledgeUiState())
    val uiState: StateFlow<KnowledgeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.warm()
            repo.entries.collect { entries ->
                _uiState.value = _uiState.value.copy(
                    entries = entries,
                    nameOfTheDay = repo.nameOfTheDay()
                )
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun selectSection(section: KnowledgeKind?) {
        _uiState.value = _uiState.value.copy(section = section)
    }

    fun toggleFaq(id: String) {
        _uiState.value = _uiState.value.copy(
            expandedFaq = if (_uiState.value.expandedFaq == id) null else id
        )
    }

    fun speak(entry: KnowledgeEntry) = tts.speak("${entry.title}. ${entry.body}")

    fun openUrl(url: String, context: Context) {
        val view = Intent(Intent.ACTION_VIEW, url.toUri())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(view)
    }

    override fun onCleared() {
        tts.stop()
        super.onCleared()
    }
}
