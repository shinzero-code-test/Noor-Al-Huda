package com.exapps.nooralhuda.feature.quran.domain

import kotlinx.coroutines.flow.Flow

interface QuranRepository {
    val surahs: Flow<List<Surah>>
    suspend fun refreshSurahs(): Result<Unit>
    suspend fun surahVerses(surahId: Int, expectedCount: Int): Result<List<Verse>>

    companion object {
        const val DEFAULT_RECITER_ID = "mishary-alafasy"
        fun reciterAudioUrl(surahId: Int): String =
            "https://server8.mp3quran.net/afs/${surahId.toString().padStart(3, '0')}.mp3"
    }
}
