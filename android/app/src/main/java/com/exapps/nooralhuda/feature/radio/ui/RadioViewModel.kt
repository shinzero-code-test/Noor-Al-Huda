package com.exapps.nooralhuda.feature.radio.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exapps.nooralhuda.core.media.AudioPlayer
import com.exapps.nooralhuda.feature.radio.domain.RadioRepository
import com.exapps.nooralhuda.feature.radio.domain.RadioStation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RadioUiState(
    val stations: List<RadioStation> = emptyList(),
    val selectedId: String? = null,
    val isPlaying: Boolean = false,
    val connected: Boolean = false,
    /** Active sleep timer option in minutes; null = off. */
    val sleepMinutes: Int? = null,
    /** Remaining sleep minutes, refreshed by the ticker. */
    val sleepRemaining: Int? = null
)

@HiltViewModel
class RadioViewModel @Inject constructor(
    private val repo: RadioRepository,
    private val audio: AudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(RadioUiState())
    val uiState: StateFlow<RadioUiState> = _uiState.asStateFlow()

    private var sleepJob: Job? = null
    private var tickerJob: Job? = null
    private var sleepEndsAt: Long = 0L

    init {
        audio.connect()
        viewModelScope.launch {
            val last = repo.lastStationId()
            combine(repo.stations, audio.state) { stations, player ->
                val current = _uiState.value
                val selected = current.selectedId
                    ?: stations.firstOrNull { it.id == last }?.id
                    ?: stations.firstOrNull()?.id
                current.copy(
                    stations = stations,
                    selectedId = selected,
                    isPlaying = player.isPlaying,
                    connected = player.connected
                )
            }.collect { _uiState.value = it }
        }
        viewModelScope.launch { repo.refreshStations() }
    }

    fun select(station: RadioStation) {
        val current = _uiState.value
        if (current.selectedId == station.id && current.isPlaying) {
            audio.toggle()
            return
        }
        _uiState.value = current.copy(selectedId = station.id)
        audio.play(station.url, station.name)
        viewModelScope.launch { repo.setLastStationId(station.id) }
    }

    fun toggle() = audio.toggle()

    fun toggleFavourite(station: RadioStation) {
        viewModelScope.launch { repo.toggleFavourite(station.id) }
    }

    /** Null = off. Survives rotation; process death ends playback anyway. */
    fun setSleepTimer(minutes: Int?) {
        sleepJob?.cancel()
        tickerJob?.cancel()
        if (minutes == null) {
            sleepEndsAt = 0L
            _uiState.value = _uiState.value.copy(sleepMinutes = null, sleepRemaining = null)
            return
        }
        sleepEndsAt = System.currentTimeMillis() + minutes * 60_000L
        _uiState.value = _uiState.value.copy(sleepMinutes = minutes, sleepRemaining = minutes)
        sleepJob = viewModelScope.launch {
            delay(minutes * 60_000L)
            audio.stop()
            _uiState.value = _uiState.value.copy(sleepMinutes = null, sleepRemaining = null)
        }
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(30_000L)
                val remaining =
                    ((sleepEndsAt - System.currentTimeMillis()) / 60_000L).toInt().coerceAtLeast(1)
                _uiState.value = _uiState.value.copy(sleepRemaining = remaining)
            }
        }
    }

    fun shareStation(station: RadioStation, context: Context) {
        val send = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, "${station.name}\n${station.url}")
        context.startActivity(
            Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun retry() {
        viewModelScope.launch { repo.refreshStations() }
    }
}
