package com.exapps.nooralhuda.feature.quran.data

import android.content.Context
import com.exapps.nooralhuda.core.network.BackendApi
import com.exapps.nooralhuda.core.privacy.PrivacyManager
import com.exapps.nooralhuda.feature.quran.domain.ChaptersResponse
import com.exapps.nooralhuda.feature.quran.domain.QuranRepository
import com.exapps.nooralhuda.feature.quran.domain.Surah
import com.exapps.nooralhuda.feature.quran.domain.SurahDto
import com.exapps.nooralhuda.feature.quran.domain.Verse
import com.exapps.nooralhuda.feature.quran.domain.VersesResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache-first Quran corpus. Room is the source of truth; the backend proxy
 * refills it; a bundled 114-surah asset seeds metadata offline.
 * No network under privacy mode — cached or bundled data only.
 */
@Singleton
class RoomQuranRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: BackendApi,
    private val surahDao: SurahDao,
    private val verseDao: VerseDao,
    private val privacy: PrivacyManager
) : QuranRepository {
    private val json = Json { ignoreUnknownKeys = true }

    private val _surahs = MutableStateFlow<List<Surah>>(emptyList())
    override val surahs: Flow<List<Surah>> = _surahs.asStateFlow()

    override suspend fun refreshSurahs(): Result<Unit> = runCatching {
        if (privacy.canFetchRemote()) {
            // Any failure (network, auth, upstream) falls through to cache/asset:
            // the list is offline-first; auth-gated features report their own errors.
            runCatching {
                val body = api.get("/api/quran/chapters")
                val chapters = json.decodeFromString<ChaptersResponse>(body).chapters
                val now = System.currentTimeMillis()
                surahDao.upsertAll(
                    chapters.map {
                        SurahEntity(
                            id = it.id,
                            arabic = it.arabic,
                            transliteration = it.transliteration,
                            english = it.translated?.name.orEmpty(),
                            verses = it.verses,
                            revelation = if (it.revelationPlace == "madinah") "medinan" else "meccan",
                            updatedAt = now
                        )
                    }
                )
            } catch (_: Exception) {
                // Fall through to cache/asset below.
            }
        }
        val cached = surahDao.all()
        _surahs.value = if (cached.isNotEmpty()) {
            cached.map { it.toDomain() }
        } else {
            bundledSurahs()
        }
    }

    override suspend fun surahVerses(surahId: Int, expectedCount: Int): Result<List<Verse>> = runCatching {
        val cached = verseDao.forSurah(surahId)
        if (cached.size >= expectedCount && expectedCount > 0) {
            return@runCatching cached.map { it.toDomain() }
        }
        if (!privacy.canFetchRemote()) {
            if (cached.isNotEmpty()) return@runCatching cached.map { it.toDomain() }
            throw IllegalStateException("Offline and not cached")
        }
        val fetched = fetchAllVerses(surahId)
        val now = System.currentTimeMillis()
        verseDao.upsertAll(
            fetched.map {
                VerseEntity(surahId, it.number, it.arabic, it.tajweed, it.translation, now)
            }
        )
        fetched
    }

    /** Paged fetch — the API caps per_page, so loop until a short page. */
    private suspend fun fetchAllVerses(surahId: Int): List<Verse> {
        val out = mutableListOf<Verse>()
        var page = 1
        while (true) {
            val body = api.get(
                "/api/quran/verses/by_chapter/$surahId" +
                    "?fields=text_uthmani,text_uthmani_tajweed&translations=20&per_page=100&page=$page"
            )
            val verses = json.decodeFromString<VersesResponse>(body).verses
            out += verses.map {
                Verse(
                    surahId = surahId,
                    number = it.number,
                    arabic = it.arabic ?: "",
                    tajweed = it.tajweed,
                    translation = it.translations.firstOrNull()?.text.orEmpty()
                )
            }
            if (verses.size < 100) break
            page += 1
        }
        if (out.isEmpty()) throw IllegalStateException("Empty verses for surah $surahId")
        return out
    }

    private fun bundledSurahs(): List<Surah> {
        val raw = context.assets.open("surahs.json").bufferedReader().use { it.readText() }
        return json.decodeFromString<List<SurahDto>>(raw).map {
            Surah(it.id, it.arabic, it.transliteration, it.english, it.verses, it.revelation)
        }
    }

    private fun SurahEntity.toDomain() = Surah(id, arabic, transliteration, english, verses, revelation)
    private fun VerseEntity.toDomain() = Verse(surahId, number, arabic, tajweed, translation)
}
