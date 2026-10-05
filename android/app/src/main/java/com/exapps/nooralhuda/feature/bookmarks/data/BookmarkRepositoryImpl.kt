package com.exapps.nooralhuda.feature.bookmarks.data

import com.exapps.nooralhuda.core.data.db.BookmarkDao
import com.exapps.nooralhuda.core.data.db.BookmarkEntity
import com.exapps.nooralhuda.core.privacy.PrivacyManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class Bookmark(
    val surahId: Int,
    val ayahNumber: Int,
    val surahName: String,
    val updatedAt: Long,
    val deleted: Boolean = false
)

/**
 * Room is the local source of truth. Firestore is remote.
 * Merge: per-item union, newest timestamp wins, deletes are tombstones.
 * Pure merge logic lives in [mergeBookmarks] for unit testing.
 */
@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val dao: BookmarkDao,
    private val firestore: FirebaseFirestore,
    private val privacy: PrivacyManager
) : BookmarkRepository {
    override suspend fun localAll(): List<Bookmark> =
        dao.all().map { it.toDomain() }

    override suspend fun toggle(bookmark: Bookmark) {
        val existing = dao.all().firstOrNull {
            it.surahId == bookmark.surahId && it.ayahNumber == bookmark.ayahNumber
        }
        val now = System.currentTimeMillis()
        if (existing == null) {
            dao.upsert(bookmark.toEntity(now).copy(deleted = false))
        } else {
            dao.upsert(existing.copy(deleted = !existing.deleted, createdAt = now))
        }
    }

    override suspend fun push(uid: String) {
        if (!privacy.canSync()) return
        val batch = firestore.batch()
        for (local in dao.all()) {
            val ref = firestore.collection("users").document(uid)
                .collection("bookmarks").document(local.key)
            if (local.deleted) {
                batch.delete(ref)
            } else {
                batch.set(
                    ref,
                    mapOf(
                        "surahId" to local.surahId,
                        "ayahNumber" to local.ayahNumber,
                        "surahName" to local.surahName,
                        "createdAt" to local.createdAt,
                        "updatedAt" to local.createdAt,
                        "syncedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
        }
        batch.commit().await()
        dao.clearDeleted()
    }

    override suspend fun pull(uid: String) {
        if (!privacy.canSync()) return
        val snapshot = firestore.collection("users").document(uid)
            .collection("bookmarks").get().await()
        val remote = snapshot.documents.mapNotNull { doc ->
            val surahId = (doc.getLong("surahId") ?: return@mapNotNull null).toInt()
            val ayah = (doc.getLong("ayahNumber") ?: return@mapNotNull null).toInt()
            Bookmark(
                surahId = surahId,
                ayahNumber = ayah,
                surahName = doc.getString("surahName").orEmpty(),
                updatedAt = doc.getLong("updatedAt") ?: 0L
            )
        }
        val merged = mergeBookmarks(localAll(), remote)
        for (item in merged) dao.upsert(item.toEntity(item.updatedAt))
    }

    private fun Bookmark.toEntity(now: Long) = BookmarkEntity(
        key = BookmarkEntity.keyOf(surahId, ayahNumber),
        surahId = surahId,
        ayahNumber = ayahNumber,
        surahName = surahName,
        createdAt = now
    )

    private fun BookmarkEntity.toDomain() = Bookmark(
        surahId = surahId,
        ayahNumber = ayahNumber,
        surahName = surahName,
        updatedAt = createdAt,
        deleted = deleted
    )
}

/** Newest timestamp wins per key. Remote has no tombstones here — local deletes win on push. */
fun mergeBookmarks(local: List<Bookmark>, remote: List<Bookmark>): List<Bookmark> {
    val byKey = LinkedHashMap<String, Bookmark>()
    for (item in remote) byKey["${item.surahId}:${item.ayahNumber}"] = item
    for (item in local) {
        val key = "${item.surahId}:${item.ayahNumber}"
        val existing = byKey[key]
        if (existing == null || item.updatedAt >= existing.updatedAt) byKey[key] = item
    }
    return byKey.values.toList()
}
