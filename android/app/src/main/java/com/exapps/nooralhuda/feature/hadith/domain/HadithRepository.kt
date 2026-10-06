package com.exapps.nooralhuda.feature.hadith.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import androidx.paging.PagingData

interface HadithRepository {
    /** Collections: static seed first, refreshed from the API when allowed. */
    val collections: StateFlow<List<HadithCollection>>

    /** Refresh the collections cache (no-op offline; falls back silently). */
    suspend fun refreshCollections(): Result<Unit>

    /** Paged items for one collection, served from Room via RemoteMediator. */
    fun itemsPager(collectionId: String): Flow<PagingData<HadithItem>>

    /** Detail: cache first, network when allowed (English optional). */
    suspend fun detail(id: String): Result<HadithDetail>

    /**
     * Report a hadith for review. Requires a signed-in user (rules enforce
     * reporter_uid == auth.uid); privacy mode and guests get a clean failure.
     */
    suspend fun reportHadith(contentId: String, reason: String): Result<Unit>
}
