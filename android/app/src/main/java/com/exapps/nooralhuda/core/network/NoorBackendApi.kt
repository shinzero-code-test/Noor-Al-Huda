package com.exapps.nooralhuda.core.network

import com.exapps.nooralhuda.feature.auth.domain.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

class BackendException(val code: Int, message: String) : Exception(message)

/**
 * Authenticated client for the admin REST proxy. Direct keyless upstreams
 * (AlAdhan, mp3quran, OpenFoodFacts…) are called elsewhere, never here.
 */
@Singleton
class NoorBackendApi @Inject constructor(
    private val auth: AuthRepository
) : BackendApi {
    private val baseUrl = "https://nooralhuda-admin-api.shinzero.workers.dev"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val json = Json { ignoreUnknownKeys = true }

    override suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        val token = auth.currentIdToken() ?: throw BackendException(401, "Not signed in")
        val request = Request.Builder()
            .url(baseUrl + path)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw BackendException(response.code, body.take(200))
            body
        }
    }
}
