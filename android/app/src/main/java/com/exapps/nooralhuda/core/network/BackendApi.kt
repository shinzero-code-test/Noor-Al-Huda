package com.exapps.nooralhuda.core.network

/** Testable seam for the admin REST proxy client. */
interface BackendApi {
    @Throws(BackendException::class)
    suspend fun get(path: String): String
}
