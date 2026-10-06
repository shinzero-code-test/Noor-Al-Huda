package com.exapps.nooralhuda.feature.radio.domain

import kotlinx.coroutines.flow.StateFlow

interface RadioRepository {
    /** Stations: fallback seed first, directory refresh when allowed. */
    val stations: StateFlow<List<RadioStation>>

    /** Refresh the directory cache (no-op offline; falls back silently). */
    suspend fun refreshStations(): Result<Unit>

    /** Toggle a station favourite (preserved across directory refreshes). */
    suspend fun toggleFavourite(id: String)

    /** Last-played station id, for resume. Null when never played. */
    suspend fun lastStationId(): String?

    /** Remember the last-played station. */
    suspend fun setLastStationId(id: String)
}
