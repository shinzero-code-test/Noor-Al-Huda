package com.exapps.nooralhuda.feature.quran.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.exapps.nooralhuda.core.media.AudioPlayer
import com.exapps.nooralhuda.core.navigation.SurahDetail
import com.exapps.nooralhuda.feature.bookmarks.data.Bookmark
import com.exapps.nooralhuda.feature.bookmarks.data.BookmarkRepository
import com.exapps.nooralhuda.feature.quran.data.AudioDownloads
import com.exapps.nooralhuda.feature.quran.domain.QuranRepository
import com.exapps.nooralhuda.feature.quran.domain.Verse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SurahDetailUiState(
    val loading: Boolean = true,
    val surahId: Int = 1,
    val verses: List<Verse> = emptyList(),
    val showTranslation: Boolean = true,
    val showTajweed: Boolean = true,
    val bookmarkedKeys: Set<String> = emptySet(),
    val audioLabel: String? = null,
    val isPlaying: Boolean = false,
    val downloaded: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SurahDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: QuranRepository,
    private val bookmarks: BookmarkRepository,
    private val audio: AudioPlayer,
    private val downloads: AudioDownloads
) : ViewModel() {

    private val surahId: Int = savedStateHandle.toRoute<SurahDetail>().surahId

    private val _uiState = MutableStateFlow(SurahDetailUiState(surahId = surahId))
    val uiState: StateFlow<SurahDetailUiState> = _uiState.asStateFlow()

    init {
        audio.connect()
        viewModelScope.launch {
            audio.state.collect { player ->
                _uiState.value = _uiState.value.copy(isPlaying = player.isPlaying)
            }
        }
        viewModelScope.launch {
            bookmarks.localAll()
                .filter { it.surahId == surahId && !it.deleted }
                .map { "${it.surahId}:${it.ayahNumber}" }
                .toSet()
                .let { _uiState.value = _uiState.value.copy(bookmarkedKeys = it) }
        }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val idle = _uiState.value
            repo.refreshSurahs()
            val expected = repo.surahs.first().firstOrNull { it.id == surahId }?.verses ?: 0
            repo.surahVerses(surahId, expected)
                .onSuccess { verses ->
                    if (verses.isEmpty()) {
                        _uiState.value = idle.copy(loading = false, error = "Empty")
                    } else {
                        _uiState.value = idle.copy(loading = false, verses = verses)
                    }
                }
                .onFailure { error ->
                    _uiState.value = idle.copy(loading = false, error = error.message)
                }
            val local = downloads.downloadedUri(QuranRepository.DEFAULT_RECITER_ID, surahId)
            if (local != null) {
                _uiState.value = _uiState.value.copy(downloaded = true)
            }
        }
    }

    fun toggleTranslation() {
        _uiState.value = _uiState.value.copy(showTranslation = !_uiState.value.showTranslation)
    }

    fun toggleTajweed() {
        _uiState.value = _uiState.value.copy(showTajweed = !_uiState.value.showTajweed)
    }

    fun toggleBookmark(verse: Verse) {
        viewModelScope.launch {
            val surahName = repo.surahs.first()
                .firstOrNull { it.id == surahId }?.arabic.orEmpty()
            bookmarks.toggle(
                Bookmark(verse.surahId, verse.number, surahName, System.currentTimeMillis())
            )
            val keys = bookmarks.localAll()
                .filter { it.surahId == surahId && !it.deleted }
                .map { "${it.surahId}:${it.ayahNumber}" }
                .toSet()
            _uiState.value = _uiState.value.copy(bookmarkedKeys = keys)
        }
    }

    fun play(label: String) {
        viewModelScope.launch {
            val local = downloads.downloadedUri(QuranRepository.DEFAULT_RECITER_ID, surahId)
            val uri = local ?: QuranRepository.reciterAudioUrl(surahId)
            _uiState.value = _uiState.value.copy(audioLabel = label)
            audio.play(uri, label)
        }
    }

    fun togglePlay() = audio.toggle()

    fun download() {
        viewModelScope.launch {
            downloads.download(
                QuranRepository.DEFAULT_RECITER_ID,
                surahId,
                QuranRepository.reciterAudioUrl(surahId)
            ).onSuccess {
                _uiState.value = _uiState.value.copy(downloaded = true)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
            }
        }
    }
}
