package com.exapps.nooralhuda.core.media

import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {
    val state: kotlinx.coroutines.flow.StateFlow<PlayerState>
    fun connect()
    fun play(uri: String, label: String)
    fun toggle()
    fun stop()
    fun release()
}
