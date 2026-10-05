package com.exapps.nooralhuda.feature.prayer.data

import com.exapps.nooralhuda.core.privacy.PrivacyManager
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class AzkarEntry(val id: String, val text: String, val count: Int, val virtue: String)

/**
 * HisnMuslim catalog: direct keyless fetch + Room cache + bundled virtue text.
 * Fully offline after first load.
 */
@Singleton
class AzkarRepository @Inject constructor(
    private val dao: AzkarDao,
    private val privacy: PrivacyManager
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun collection(name: String): List<AzkarEntry> {
        val cached = dao.forCollection(name)
        if (cached.isNotEmpty()) return cached.map { it.toDomain() }
        if (!privacy.canFetchRemote()) return emptyList()
        return try {
            val categoryId = if (name == "after-prayer") 25 else 27
            val request = Request.Builder()
                .url("https://www.hisnmuslim.com/api/ar/$categoryId.json")
                .build()
            val raw = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}")
                response.body?.string().orEmpty().trimStart('\uFEFF')
            }
            val items = extract(raw, name)
            val now = System.currentTimeMillis()
            dao.upsertAll(
                items.map {
                    AzkarEntity(name, it.id, it.text, it.count, it.virtue, now)
                }
            )
            items
        } catch (_: Exception) {
            dao.forCollection(name).map { it.toDomain() }
        }
    }

    private fun extract(raw: String, collection: String): List<AzkarEntry> {
        val root = json.parseToJsonElement(raw) as? kotlinx.serialization.json.JsonObject
            ?: return emptyList()
        val first = root.values.firstOrNull() as? kotlinx.serialization.json.JsonArray
            ?: return emptyList()
        val virtue = if (collection == "after-prayer") "Dhikr after prayer." else "Dhikr."
        return first.mapNotNull { element ->
            val obj = element as? kotlinx.serialization.json.JsonObject ?: return@mapNotNull null
            val id = obj["ID"]?.toString()?.trim('"') ?: return@mapNotNull null
            val text = (obj["ARABIC_TEXT"] as? kotlinx.serialization.json.JsonPrimitive)
                ?.takeIf { it.isString }?.content ?: return@mapNotNull null
            val count = (obj["REPEAT"] as? kotlinx.serialization.json.JsonPrimitive)
                ?.content?.toIntOrNull() ?: 1
            AzkarEntry(id, text, count, virtue)
        }.take(40)
    }

    private fun AzkarEntity.toDomain() = AzkarEntry(entryId, text, count, virtue)
}
