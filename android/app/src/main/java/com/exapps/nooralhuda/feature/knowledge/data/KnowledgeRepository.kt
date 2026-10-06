package com.exapps.nooralhuda.feature.knowledge.data

import com.exapps.nooralhuda.feature.knowledge.domain.KnowledgeEntry
import com.exapps.nooralhuda.feature.knowledge.domain.KnowledgeKind
import com.exapps.nooralhuda.feature.knowledge.domain.KnowledgeRepository
import com.exapps.nooralhuda.feature.knowledge.domain.STATIC_KNOWLEDGE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fully offline knowledge corpus. The static entries seed Room on first
 * run. No network involved at all.
 */
@Singleton
class RoomKnowledgeRepository @Inject constructor(
    private val dao: KnowledgeEntryDao
) : KnowledgeRepository {

    private val _entries = MutableStateFlow(STATIC_KNOWLEDGE)
    override val entries: StateFlow<List<KnowledgeEntry>> = _entries.asStateFlow()

    override suspend fun warm() {
        if (dao.count() == 0) {
            dao.insertAll(
                STATIC_KNOWLEDGE.map {
                    KnowledgeEntryEntity(it.id, it.kind.name, it.title, it.subtitle, it.body, it.url)
                }
            )
        }
        _entries.value = dao.all().map { it.toDomain() }
    }

    private fun KnowledgeEntryEntity.toDomain() = KnowledgeEntry(
        id = id,
        kind = runCatching { KnowledgeKind.valueOf(kind) }.getOrDefault(KnowledgeKind.FAQ),
        title = title,
        subtitle = subtitle,
        body = body,
        url = url
    )
}
