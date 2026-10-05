package com.exapps.nooralhuda.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Arabic TTS. On-device preferred; availability is surfaced, never assumed. */
@Singleton
class NoorTts @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    fun ensure() {
        if (tts != null) return
        tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                _ready.value = false
                return@TextToSpeech
            }
            val tts = tts ?: return@TextToSpeech
            val arabic = tts.setLanguage(Locale("ar"))
            val fallback = if (arabic == TextToSpeech.LANG_MISSING_DATA ||
                arabic == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                tts.setLanguage(Locale.UK)
            } else arabic
            _ready.value = fallback == TextToSpeech.LANG_AVAILABLE ||
                fallback == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                fallback == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
        }
    }

    fun speak(text: String) {
        ensure()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "noor-${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }
}
