package com.exapps.nooralhuda.feature.seerah.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.exapps.nooralhuda.core.navigation.SeerahReader
import com.exapps.nooralhuda.core.tts.NoorTts
import com.exapps.nooralhuda.feature.seerah.domain.SeerahChapter
import com.exapps.nooralhuda.feature.seerah.domain.SeerahRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeerahListUiState(
    val chapters: List<SeerahChapter> = emptyList()
)

@HiltViewModel
class SeerahListViewModel @Inject constructor(
    private val repo: SeerahRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeerahListUiState())
    val uiState: StateFlow<SeerahListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.warm()
            repo.chapters.collect { chapters ->
                _uiState.value = SeerahListUiState(chapters)
            }
        }
    }
}

data class SeerahReaderUiState(
    val chapter: SeerahChapter? = null
)

@HiltViewModel
class SeerahReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: SeerahRepository,
    private val tts: NoorTts
) : ViewModel() {

    private val chapterId: String = savedStateHandle.toRoute<SeerahReader>().chapterId

    private val _uiState = MutableStateFlow(SeerahReaderUiState())
    val uiState: StateFlow<SeerahReaderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.warm()
            _uiState.value = SeerahReaderUiState(repo.chapter(chapterId))
        }
    }

    fun speak() {
        _uiState.value.chapter?.let { chapter ->
            tts.speak("${chapter.title}. ${chapter.summary} ${chapter.reflection}")
        }
    }

    override fun onCleared() {
        tts.stop()
        super.onCleared()
    }
}
