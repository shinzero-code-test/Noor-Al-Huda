package com.exapps.nooralhuda.feature.daily.domain

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.StateFlow

/** Verse + hadith of the day with an offline fallback. */
@Immutable
data class DailyContent(
    val surahId: Int,
    val verseRef: String,
    val verseArabic: String,
    val verseTranslation: String,
    val hadithId: String,
    val hadithTitle: String,
    val hadithText: String,
    val hadithSource: String
)

interface DailyContentRepository {
    val content: StateFlow<DailyContent>
    suspend fun refresh()
}

/**
 * Offline fallback (ported from the legacy client). Content data,
 * not UI text — hence plain data, not string resources.
 */
val FALLBACK_DAILY = DailyContent(
    surahId = 2,
    verseRef = "2:186",
    verseArabic = "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ ۖ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ",
    verseTranslation = "When My servants ask you about Me — I am near. I answer the call of the caller when he calls upon Me.",
    hadithId = "daily-hadith",
    hadithTitle = "حديث اليوم",
    hadithText = "قال رسول الله صلى الله عليه وسلم: أحب الأعمال إلى الله أدومها وإن قل.",
    hadithSource = "متفق عليه"
)
