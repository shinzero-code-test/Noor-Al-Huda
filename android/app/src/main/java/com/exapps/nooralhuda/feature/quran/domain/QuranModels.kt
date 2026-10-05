package com.exapps.nooralhuda.feature.quran.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SurahDto(
    val id: Int,
    @SerialName("name_arabic") val arabic: String = "",
    val transliteration: String = "",
    @SerialName("englishName") val english: String = "",
    @SerialName("versesCount") val verses: Int = 0,
    val revelation: String = ""
)

@Serializable
data class ChaptersResponse(val chapters: List<QfChapter> = emptyList())

@Serializable
data class QfChapter(
    val id: Int,
    @SerialName("name_arabic") val arabic: String = "",
    @SerialName("name_simple") val transliteration: String = "",
    @SerialName("translated_name") val translated: QfTranslatedName? = null,
    @SerialName("verses_count") val verses: Int = 0,
    @SerialName("revelation_place") val revelationPlace: String = ""
)

@Serializable
data class QfTranslatedName(val name: String = "")

@Serializable
data class VersesResponse(val verses: List<QfVerse> = emptyList())

@Serializable
data class QfVerse(
    @SerialName("verse_number") val number: Int = 0,
    @SerialName("verse_key") val key: String = "",
    @SerialName("text_uthmani") val arabic: String? = null,
    @SerialName("text_uthmani_tajweed") val tajweed: String? = null,
    val translations: List<QfTranslation> = emptyList()
)

@Serializable
data class QfTranslation(val text: String = "")

data class Surah(
    val id: Int,
    val arabic: String,
    val transliteration: String,
    val english: String,
    val verses: Int,
    val revelation: String
)

data class Verse(
    val surahId: Int,
    val number: Int,
    val arabic: String,
    val tajweed: String?,
    val translation: String
)
