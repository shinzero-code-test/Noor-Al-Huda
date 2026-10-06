package com.exapps.nooralhuda.feature.seerah.data

import com.exapps.nooralhuda.feature.seerah.domain.STATIC_SEERAH_CHAPTERS
import com.exapps.nooralhuda.feature.seerah.domain.SeerahChapter
import com.exapps.nooralhuda.feature.seerah.domain.SeerahRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fully offline biography corpus. The static chapters seed Room on first
 * run. No network involved at all.
 */
@Singleton
class RoomSeerahRepository @Inject constructor(
    private val dao: SeerahChapterDao
) : SeerahRepository {

    private val _chapters = MutableStateFlow(STATIC_SEERAH_CHAPTERS)
    override val chapters: StateFlow<List<SeerahChapter>> = _chapters.asStateFlow()

    override suspend fun warm() {
        if (dao.count() == 0) {
            dao.insertAll(
                STATIC_SEERAH_CHAPTERS.map {
                    SeerahChapterEntity(
                        id = it.id,
                        title = it.title,
                        summary = it.summary,
                        reflection = it.reflection,
                        lessons = it.lessons.joinToString("\u001F")
                    )
                }
            )
        }
        _chapters.value = dao.all().map { it.toDomain() }
    }

    override suspend fun chapter(id: String): SeerahChapter? =
        _chapters.value.firstOrNull { it.id == id }

    private fun SeerahChapterEntity.toDomain() = SeerahChapter(
        id = id,
        title = title,
        summary = summary,
        reflection = reflection,
        lessons = lessons.split("\u001F").filter { it.isNotBlank() }
    )
}
