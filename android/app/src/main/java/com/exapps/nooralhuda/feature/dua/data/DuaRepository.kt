package com.exapps.nooralhuda.feature.dua.data

import com.exapps.nooralhuda.feature.dua.domain.DuaEntry
import com.exapps.nooralhuda.feature.dua.domain.DuaRepository
import com.exapps.nooralhuda.feature.dua.domain.STATIC_DUA_CATALOG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fully offline dua catalog. The static corpus seeds Room on first run;
 * favourites persist locally. No network involved at all.
 */
@Singleton
class RoomDuaRepository @Inject constructor(
    private val dao: DuaEntryDao
) : DuaRepository {

    private val _duas = MutableStateFlow<List<DuaEntry>>(STATIC_DUA_CATALOG)
    override val duas: StateFlow<List<DuaEntry>> = _duas.asStateFlow()

    override suspend fun warm() {
        if (dao.count() == 0) {
            dao.insertAll(
                STATIC_DUA_CATALOG.map {
                    DuaEntryEntity(
                        id = it.id,
                        category = it.category.name,
                        arabic = it.arabic,
                        transliteration = it.transliteration,
                        translation = it.translation,
                        repeat = it.repeat,
                        source = it.source
                    )
                }
            )
        }
        _duas.value = dao.all().map { it.toDomain() }
    }

    override suspend fun toggleFavourite(id: String) {
        dao.toggleFavourite(id)
        _duas.value = dao.all().map { it.toDomain() }
    }

    private fun DuaEntryEntity.toDomain() = DuaEntry(
        id = id,
        category = runCatching {
            com.exapps.nooralhuda.feature.dua.domain.DuaCategory.valueOf(category)
        }.getOrDefault(com.exapps.nooralhuda.feature.dua.domain.DuaCategory.MORNING),
        arabic = arabic,
        transliteration = transliteration,
        translation = translation,
        repeat = repeat,
        source = source,
        favourite = favourite
    )
}
