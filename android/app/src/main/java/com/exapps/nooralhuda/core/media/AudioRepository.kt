package com.exapps.nooralhuda.core.media

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class PlayerState(
    val isPlaying: Boolean = false,
    val label: String? = null,
    val connected: Boolean = false
)

/** UI-facing playback. Binds to PlaybackService; survives rotation and backgrounding. */
@Singleton
class AudioRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(isPlaying = isPlaying)
        }
    }

    override fun connect() {
        if (controllerFuture != null) return
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, token).buildAsync().also { future ->
            scope.launch {
                try {
                    controller = future.await().also {
                        it.addListener(listener)
                        _state.value = _state.value.copy(
                            connected = true,
                            isPlaying = it.isPlaying
                        )
                    }
                } catch (_: Exception) {
                    _state.value = _state.value.copy(connected = false)
                }
            }
        }
    }

    override fun play(uri: String, label: String) {
        scope.launch {
            val c = controller ?: return@launch
            _state.value = _state.value.copy(label = label)
            c.setMediaItem(MediaItem.fromUri(uri))
            c.prepare()
            c.play()
        }
    }

    override fun toggle() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    override fun stop() {
        controller?.stop()
        _state.value = _state.value.copy(isPlaying = false)
    }

    override fun release() {
        controller?.removeListener(listener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        controller = null
    }
}
