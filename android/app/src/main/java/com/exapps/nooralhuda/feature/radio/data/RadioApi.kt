package com.exapps.nooralhuda.feature.radio.data

import com.exapps.nooralhuda.core.di.IoDispatcher
import com.exapps.nooralhuda.feature.radio.domain.Mp3QuranRadiosResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Direct keyless mp3quran.net client (no backend, no secrets).
 * Timeouts on every call.
 */
@Singleton
class RadioApi @Inject constructor(
    @IoDispatcher private val io: CoroutineDispatcher
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val base = "https://www.mp3quran.net/api/v3/".toHttpUrl()

    suspend fun directory(lang: String): Mp3QuranRadiosResponse = withContext(io) {
        val url = base.newBuilder()
            .addPathSegments("radios")
            .addQueryParameter("language", lang)
            .build()
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "NoorAlHuda/1.0 (Android)")
            .header("Accept", "application/json")
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw RadioApiException(response.code)
            json.decodeFromString<Mp3QuranRadiosResponse>(body)
        }
    }
}

class RadioApiException(val code: Int) : Exception("mp3quran radios failed: $code")
