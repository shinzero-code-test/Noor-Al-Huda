package com.exapps.nooralhuda.feature.hadith.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.exapps.nooralhuda.core.privacy.PrivacyManager
import com.exapps.nooralhuda.feature.hadith.domain.HadithItem

/**
 * Page-keyed source over one hadeethenc collection with a Room page cache.
 *
 * Room stays out of Paging's processing path on purpose: the DAO exposes
 * plain LIMIT/OFFSET queries (see HadithItemDao) and this class owns the
 * network-then-cache policy. Offline or privacy mode serves cache slices
 * only and stops paginating — no error state, no network.
 */
class HadithPagingSource(
    private val collectionId: String,
    private val lang: String,
    private val perPage: Int,
    private val api: HadeethencApi,
    private val items: HadithItemDao,
    private val privacy: PrivacyManager
) : PagingSource<Int, HadithItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, HadithItem> {
        val page = params.key ?: 1
        // Privacy/offline: cache slices only.
        if (!privacy.canFetchRemote()) {
            val cached = items.pageByCollection(
                collectionId,
                limit = params.loadSize,
                offset = (page - 1) * params.loadSize
            )
            return LoadResult.Page(
                data = cached.map { HadithItem(it.id, it.collectionId, it.title) },
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (cached.size < params.loadSize) null else page + 1
            )
        }
        return try {
            val response = api.listItems(collectionId, page, perPage, lang)
            val entities = response.data.mapIndexed { index, dto ->
                HadithItemEntity(
                    collectionId = collectionId,
                    id = dto.id,
                    title = dto.title,
                    sortKey = page * 1000 + index
                )
            }
            if (page == 1) items.clearByCollection(collectionId)
            items.upsertAll(entities)
            val endReached = page >= response.meta.last_page || entities.isEmpty()
            LoadResult.Page(
                data = entities.map { HadithItem(it.id, it.collectionId, it.title) },
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (endReached) null else page + 1
            )
        } catch (e: Exception) {
            // Network failed: fall back to whatever this page holds in cache.
            val cached = try {
                items.pageByCollection(
                    collectionId,
                    limit = params.loadSize,
                    offset = (page - 1) * params.loadSize
                )
            } catch (ignored: Exception) {
                return LoadResult.Error(e)
            }
            if (cached.isEmpty()) LoadResult.Error(e)
            else LoadResult.Page(
                data = cached.map { HadithItem(it.id, it.collectionId, it.title) },
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (cached.size < params.loadSize) null else page + 1
            )
        }
    }

    override fun getRefreshKey(state: PagingState<Int, HadithItem>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.let { page ->
                page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
            }
        }
}
