package com.exapps.nooralhuda.feature.quran.data

import android.content.Context
import com.exapps.nooralhuda.core.privacy.PrivacyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Surah audio files (mp3quran CDN, follows redirects). Streams by URL;
 * downloads persist to internal storage and are tracked in Room.
 */
@Singleton
class AudioDownloadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloads: ReciterDownloadDao,
    private val privacy: PrivacyManager
) : AudioDownloads {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun downloadedUri(reciterId: String, surahId: Int): String? =
        downloads.fileUri(reciterId, surahId)

    override suspend fun download(reciterId: String, surahId: Int, url: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (!privacy.canFetchRemote()) throw IllegalStateException("Offline")
                val dir = File(context.filesDir, "audio/$reciterId").apply { mkdirs() }
                val out = File(dir, "$surahId.mp3")
                if (out.exists() && out.length() > 0) {
                    return@runCatching record(reciterId, surahId, out)
                }
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}")
                    val body = response.body ?: throw IllegalStateException("Empty body")
                    out.outputStream().use { sink -> body.byteStream().copyTo(sink) }
                }
                record(reciterId, surahId, out)
            }
        }

    private suspend fun record(reciterId: String, surahId: Int, file: File): String {
        val uri = "file://${file.absolutePath}"
        downloads.upsert(
            ReciterDownloadEntity(reciterId, surahId, uri, System.currentTimeMillis())
        )
        return uri
    }
}
