package com.exapps.nooralhuda.core.navigation

import kotlinx.serialization.Serializable

/** Type-safe destinations. 6 tabs + 2 detail routes in A1; feature graph grows per slice. */
@Serializable
data object Home

@Serializable
data object Quran

@Serializable
data object Prayer

@Serializable
data object Azkar

@Serializable
data object Radio

@Serializable
data object Hadith

@Serializable
data object Settings

@Serializable
data class SurahDetail(val surahId: Int)

@Serializable
data class HadithReader(val hadithId: String)

@Serializable
data object Auth

@Serializable
data object Qibla

@Serializable
data object Tracker
