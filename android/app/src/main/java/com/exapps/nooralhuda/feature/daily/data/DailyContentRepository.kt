package com.exapps.nooralhuda.feature.daily.data

import com.exapps.nooralhuda.core.privacy.PrivacyManager
import com.exapps.nooralhuda.feature.daily.domain.DailyContent
import com.exapps.nooralhuda.feature.daily.domain.DailyContentRepository
import com.exapps.nooralhuda.feature.daily.domain.FALLBACK_DAILY
import com.exapps.nooralhuda.feature.hadith.data.HadeethencApi
import com.exapps.nooralhuda.feature.quran.domain.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Verse + hadith of the day. Rotation matches the legacy client
 * (surah by day-of-year, Bukhari page by day-of-year); verses resolve
 * through the Room-cached Quran corpus, hadith through hadeethenc with
 * the detail cache. Privacy mode or any failure yields the fallback —
 * the Home sections never error, they degrade.
 */
@Singleton
class RoomDailyContentRepository @Inject constructor(
    private val quran: QuranRepository,
    private val hadith: HadeethencApi,
    private val privacy: PrivacyManager
) : DailyContentRepository {

    private val _content = MutableStateFlow(FALLBACK_DAILY)
    override val content: StateFlow<DailyContent> = _content.asStateFlow()

    override suspend fun refresh() {
        if (!privacy.canFetchRemote()) {
            _content.value = FALLBACK_DAILY
            return
        }
        _content.value = try {
            fetchDaily()
        } catch (ignored: Exception) {
            FALLBACK_DAILY
        }
    }

    private suspend fun fetchDaily(): DailyContent {
        val dayOfYear = ChronoUnit.DAYS.between(
            LocalDate.of(LocalDate.now().year, 1, 1), LocalDate.now()
        ).toInt().coerceAtLeast(0) + 1
        val lang = if (Locale.getDefault().language == "ar") "ar" else "en"

        val surahId = (dayOfYear % 114) + 1
        quran.refreshSurahs()
        val surahs = quran.surahs.first { it.isNotEmpty() }
        val surah = surahs.firstOrNull { it.id == surahId } ?: surahs.first()
        val verses = quran.surahVerses(surah.id, surah.verses).getOrThrow()
        val verse = verses.firstOrNull { it.number == 1 } ?: verses.first()

        val page = (dayOfYear % 197) + 1
        val list = hadith.listItems("1", page, 1, lang)
        val summary = list.data.firstOrNull() ?: throw IllegalStateException("empty hadith page")
        val detail = hadith.one(summary.id, "ar") ?: throw IllegalStateException("missing hadith")

        return DailyContent(
            surahId = surah.id,
            verseRef = "${surah.id}:${verse.number}",
            verseArabic = verse.arabic,
            verseTranslation = verse.translation,
            hadithId = detail.id,
            hadithTitle = detail.title,
            hadithText = detail.hadeeth,
            hadithSource = detail.attribution ?: detail.grade ?: "HadeethEnc"
        )
    }
}
