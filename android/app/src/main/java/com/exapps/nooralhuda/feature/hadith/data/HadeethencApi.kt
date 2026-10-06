package com.exapps.nooralhuda.feature.hadith.data

import com.exapps.nooralhuda.core.di.IoDispatcher
import com.exapps.nooralhuda.feature.hadith.domain.CategoryDto
import com.exapps.nooralhuda.feature.hadith.domain.HadithListResponse
import com.exapps.nooralhuda.feature.hadith.domain.HadithOneDto
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
 * Direct keyless hadeethenc.com client (no backend, no secrets).
 * Timeouts on every call; blank bodies (untranslated detail) decode to null.
 */
@Singleton
class HadeethencApi @Inject constructor(
    @IoDispatcher private val io: CoroutineDispatcher
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val base = "https://hadeethenc.com/api/v1/".toHttpUrl()

    suspend fun rootCategories(lang: String): List<CategoryDto> = withContext(io) {
        val url = base.newBuilder()
            .addPathSegments("categories/roots/")
            .addQueryParameter("language", lang)
            .build()
        val body = get(url.toString())
        json.decodeFromString<List<CategoryDto>>(body)
    }

    suspend fun listItems(
        collectionId: String,
        page: Int,
        perPage: Int,
        lang: String
    ): HadithListResponse = withContext(io) {
        val url = base.newBuilder()
            .addPathSegments("hadeeths/list/")
            .addQueryParameter("language", lang)
            .addQueryParameter("category_id", collectionId)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("per_page", perPage.toString())
            .build()
        val body = get(url.toString())
        json.decodeFromString<HadithListResponse>(body)
    }

    /** Null when the translation does not exist (API returns a blank body). */
    suspend fun one(id: String, lang: String): HadithOneDto? = withContext(io) {
        val url = base.newBuilder()
            .addPathSegments("hadeeths/one/")
            .addQueryParameter("language", lang)
            .addQueryParameter("id", id)
            .build()
        val body = get(url.toString())
        if (body.isBlank()) return@withContext null
        json.decodeFromString<HadithOneDto>(body)
    }

    private fun get(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "NoorAlHuda/1.0 (Android)")
            .header("Accept", "application/json")
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw HadeethencException(response.code)
            return body
        }
    }
}

class HadeethencException(val code: Int) : Exception("hadeethenc failed: $code")
