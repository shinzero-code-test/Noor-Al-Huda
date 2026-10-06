package com.exapps.nooralhuda.feature.radio.domain

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/** One live recitation stream. */
@Immutable
data class RadioStation(
    val id: String,
    val name: String,
    val url: String,
    val favourite: Boolean = false
)

// --- mp3quran.net wire DTOs (proven against the legacy client) ---

@Serializable
data class Mp3QuranStationDto(
    val id: Int,
    val name: String,
    val url: String
)

@Serializable
data class Mp3QuranRadiosResponse(
    val radios: List<Mp3QuranStationDto>
)

/**
 * Static fallback stations (ported from the legacy client). Seed Room and
 * serve offline when the directory endpoint is unreachable. Content data,
 * not UI text — hence plain data, not string resources.
 */
val STATIC_FALLBACK_STATIONS = listOf(
    RadioStation(
        id = "makkah-live",
        name = "إذاعة القرآن من مكة",
        url = "https://server8.mp3quran.net/afs/001.mp3"
    ),
    RadioStation(
        id = "egypt-quran",
        name = "إذاعة القرآن المصرية",
        url = "https://server8.mp3quran.net/afs/002.mp3"
    ),
    RadioStation(
        id = "sunnah-radio",
        name = "إذاعة السنة النبوية",
        url = "https://server8.mp3quran.net/afs/112.mp3"
    )
)
