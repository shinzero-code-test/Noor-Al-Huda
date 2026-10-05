package com.exapps.nooralhuda.core.privacy

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Privacy modes. Privacy ON is a real offline posture, not a label. */
object PrivacyModes {
    const val FULL = "full"
    const val PRIVATE = "private"
}

/**
 * Central gate checked by every repository before any network.
 * Firebase Auth/Firestore/FCM bypass OkHttp by design, so an
 * interceptor-only gate would lie — each repository calls these.
 *
 * Privacy ON: no Firestore sync, no AI proxy, no remote search,
 * no server STT, no telemetry. Room, DataStore, offline content
 * and local prayer math keep working.
 */
@Singleton
class PrivacyManager @Inject constructor(
    private val modeFlow: @JvmSuppressWildcards Flow<String?>
) {
    val isPrivate: Flow<Boolean> = modeFlow.map { it == PrivacyModes.PRIVATE }

    suspend fun canSync(): Boolean = modeFlow.first() != PrivacyModes.PRIVATE
    suspend fun canUseAi(): Boolean = modeFlow.first() != PrivacyModes.PRIVATE
    suspend fun canUseRemoteSearch(): Boolean = modeFlow.first() != PrivacyModes.PRIVATE
    suspend fun canUseServerStt(): Boolean = modeFlow.first() != PrivacyModes.PRIVATE
    suspend fun canFetchRemote(): Boolean = modeFlow.first() != PrivacyModes.PRIVATE

    /** Allowlist mirror of the legacy canRequestUrl for the future Retrofit layer. */
    fun canRequestUrl(url: String): Boolean {
        val host = runCatching { java.net.URI(url).host ?: "" }.getOrDefault("")
        return host.endsWith("quran.foundation")
            || host.endsWith("aladhan.com")
            || host.endsWith("mp3quran.net")
            || host.endsWith("hisnmuslim.com")
            || host.endsWith("hadeethenc.com")
            || host.endsWith("dorarl.net")
            || host.endsWith("openfoodfacts.org")
            || host.endsWith("workers.dev")
            || host.endsWith("googleapis.com")
            || host.endsWith("firebaseio.com")
    }
}
