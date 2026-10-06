package com.exapps.nooralhuda.feature.radio.data

import com.exapps.nooralhuda.core.data.prefs.PreferencesStore
import com.exapps.nooralhuda.core.privacy.PrivacyManager
import com.exapps.nooralhuda.feature.radio.domain.RadioRepository
import com.exapps.nooralhuda.feature.radio.domain.RadioStation
import com.exapps.nooralhuda.feature.radio.domain.STATIC_FALLBACK_STATIONS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache-first station directory. Room is the source of truth; mp3quran
 * refills it; the static fallback seeds offline. No network under privacy
 * mode — cached or fallback data only.
 */
@Singleton
class RoomRadioRepository @Inject constructor(
    private val api: RadioApi,
    private val dao: RadioStationDao,
    private val prefs: PreferencesStore,
    private val privacy: PrivacyManager
) : RadioRepository {

    private val _stations =
        MutableStateFlow<List<RadioStation>>(STATIC_FALLBACK_STATIONS)
    override val stations: StateFlow<List<RadioStation>> = _stations.asStateFlow()

    /** Seed from Room/fallback first so the UI never waits on network. */
    suspend fun warmStations() {
        val cached = dao.all()
        _stations.value = if (cached.isNotEmpty()) {
            cached.map { RadioStation(it.id, it.name, it.url, it.favourite) }
        } else {
            STATIC_FALLBACK_STATIONS.forEach {
                dao.upsertPreserveFavourite(it.id, it.name, it.url)
            }
            STATIC_FALLBACK_STATIONS
        }
    }

    override suspend fun refreshStations(): Result<Unit> = runCatching {
        warmStations()
        if (!privacy.canFetchRemote()) return@runCatching
        try {
            val lang = if (Locale.getDefault().language == "ar") "ar" else "en"
            val radios = api.directory(lang).radios.take(24)
            if (radios.isNotEmpty()) {
                radios.forEach { dao.upsertPreserveFavourite(it.id.toString(), it.name, it.url) }
                _stations.value = dao.all()
                    .map { RadioStation(it.id, it.name, it.url, it.favourite) }
            }
        } catch (ignored: Exception) {
            // Fall through to cache/fallback — directory stays offline-first.
        }
    }

    override suspend fun toggleFavourite(id: String) {
        dao.toggleFavourite(id)
        _stations.value = dao.all()
            .map { RadioStation(it.id, it.name, it.url, it.favourite) }
    }

    override suspend fun lastStationId(): String? = prefs.lastRadioId()

    override suspend fun setLastStationId(id: String) = prefs.setLastRadioId(id)
}
