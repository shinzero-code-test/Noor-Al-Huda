package com.exapps.nooralhuda.feature.dua.domain

import kotlinx.coroutines.flow.StateFlow

interface DuaRepository {
    val duas: StateFlow<List<DuaEntry>>
    /** Seed Room from the static corpus on first run; reload afterwards. */
    suspend fun warm()
    suspend fun toggleFavourite(id: String)
}
