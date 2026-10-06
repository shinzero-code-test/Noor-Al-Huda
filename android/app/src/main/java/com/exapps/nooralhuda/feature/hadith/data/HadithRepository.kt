package com.exapps.nooralhuda.feature.hadith.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.exapps.nooralhuda.core.privacy.PrivacyManager
import com.exapps.nooralhuda.feature.hadith.domain.HadithCollection
import com.exapps.nooralhuda.feature.hadith.domain.HadithDetail
import com.exapps.nooralhuda.feature.hadith.domain.HadithItem
import com.exapps.nooralhuda.feature.hadith.domain.HadithRepository
import com.exapps.nooralhuda.feature.hadith.domain.STATIC_HADITH_COLLECTIONS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache-first hadith library. Room is the source of truth; hadeethenc refills
 * it; the static catalog seeds collections offline. No network under privacy
 * mode — cached or static data only.
 */
@OptIn(ExperimentalPagingApi::class)
@Singleton
class RoomHadithRepository @Inject constructor(
    private val api: HadeethencApi,
    private val collectionsDao: HadithCollectionDao,
    private val itemsDao: HadithItemDao,
    private val keysDao: HadithRemoteKeyDao,
    private val detailsDao: HadithDetailDao,
    private val privacy: PrivacyManager,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : HadithRepository {

    private val _collections = MutableStateFlow<List<HadithCollection>>(STATIC_HADITH_COLLECTIONS)
    override val collections: StateFlow<List<HadithCollection>> = _collections.asStateFlow()

    private fun listLang(): String =
        if (Locale.getDefault().language == "ar") "ar" else "en"

    /** Seed from Room/static first so the UI never waits on network. */
    suspend fun warmCollections() {
        val cached = collectionsDao.all()
        _collections.value = if (cached.isNotEmpty()) {
            cached.map { HadithCollection(it.id, it.title, it.count, it.group) }
        } else {
            collectionsDao.upsertAll(
                STATIC_HADITH_COLLECTIONS.map {
                    HadithCollectionEntity(it.id, it.title, it.count, it.group)
                }
            )
            STATIC_HADITH_COLLECTIONS
        }
    }

    override suspend fun refreshCollections(): Result<Unit> = runCatching {
        warmCollections()
        if (!privacy.canFetchRemote()) return@runCatching
        try {
            val roots = api.rootCategories(listLang()).take(12)
            if (roots.isNotEmpty()) {
                val merged = roots.map {
                    HadithCollectionEntity(
                        id = it.id,
                        title = it.title,
                        count = it.hadeeths_count.toIntOrNull() ?: 0,
                        group = "حسب API"
                    )
                }
                collectionsDao.upsertAll(merged)
                _collections.value = merged.map {
                    HadithCollection(it.id, it.title, it.count, it.group)
                }
            }
        } catch (ignored: Exception) {
            // Fall through to cache/static below — list stays offline-first.
        }
    }

    override fun itemsPager(collectionId: String): Flow<PagingData<HadithItem>> =
        Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 10),
            remoteMediator = HadithRemoteMediator(
                collectionId = collectionId,
                lang = listLang(),
                api = api,
                items = itemsDao,
                keys = keysDao,
                privacy = privacy
            ),
            pagingSourceFactory = { itemsDao.pagingSource(collectionId) }
        ).flow.map { paging -> paging.map { HadithItem(it.id, it.collectionId, it.title) } }

    override suspend fun detail(id: String): Result<HadithDetail> = runCatching {
        detailsDao.get(id)?.let {
            return@runCatching HadithDetail(it.id, it.title, it.arabic, it.english, it.source)
        }
        if (!privacy.canFetchRemote()) throw IllegalStateException("Offline and not cached")
        val arabic = api.one(id, "ar") ?: throw IllegalStateException("Hadith not found")
        // English is best-effort: the API returns a blank body when untranslated.
        val english = try {
            api.one(id, "en")?.hadeeth?.takeIf { it.isNotBlank() }
        } catch (ignored: Exception) {
            null
        }
        val entity = HadithDetailEntity(
            id = arabic.id,
            title = arabic.title,
            arabic = arabic.hadeeth,
            english = english,
            source = arabic.attribution ?: arabic.grade ?: "HadeethEnc",
            updatedAt = System.currentTimeMillis()
        )
        detailsDao.put(entity)
        HadithDetail(entity.id, entity.title, entity.arabic, entity.english, entity.source)
    }

    override suspend fun reportHadith(contentId: String, reason: String): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Sign in to report")
        if (!privacy.canFetchRemote()) throw IllegalStateException("Offline")
        val doc = mapOf(
            "contentId" to contentId.take(200),
            "reason" to reason.take(50),
            "details" to "hadith:$contentId",
            "reporter_uid" to uid,
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("content_flags").add(doc).await()
        Unit
    }
}
