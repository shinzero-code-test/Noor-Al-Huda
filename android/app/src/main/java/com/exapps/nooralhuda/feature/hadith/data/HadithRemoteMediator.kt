package com.exapps.nooralhuda.feature.hadith.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.exapps.nooralhuda.core.privacy.PrivacyManager

/**
 * Page-keyed mediator over one hadeethenc collection. Room is the source of
 * truth: offline or privacy mode serves cache and stops paginating.
 */
@OptIn(ExperimentalPagingApi::class)
class HadithRemoteMediator(
    private val collectionId: String,
    private val lang: String,
    private val api: HadeethencApi,
    private val items: HadithItemDao,
    private val keys: HadithRemoteKeyDao,
    private val privacy: PrivacyManager
) : RemoteMediator<Int, HadithItemEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, HadithItemEntity>
    ): MediatorResult {
        // Privacy/offline: serve Room only, no network, no error state.
        if (!privacy.canFetchRemote()) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val key = keys.get(collectionId)
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                key.nextPage
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }
        return try {
            val response = api.listItems(collectionId, page, state.config.pageSize, lang)
            val entities = response.data.mapIndexed { index, dto ->
                HadithItemEntity(
                    collectionId = collectionId,
                    id = dto.id,
                    title = dto.title,
                    sortKey = page * 1000 + index
                )
            }
            if (loadType == LoadType.REFRESH) {
                items.clearByCollection(collectionId)
                keys.clear(collectionId)
            }
            items.upsertAll(entities)
            val endReached = page >= response.meta.last_page || entities.isEmpty()
            keys.put(
                HadithRemoteKeyEntity(
                    collectionId = collectionId,
                    nextPage = if (endReached) null else page + 1
                )
            )
            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
