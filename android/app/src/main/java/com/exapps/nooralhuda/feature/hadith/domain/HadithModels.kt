package com.exapps.nooralhuda.feature.hadith.domain

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/** A hadith collection (Kutub al-Sittah + approved compilations). */
@Immutable
data class HadithCollection(
    val id: String,
    val title: String,
    val count: Int,
    val group: String
)

/** One row in a collection listing. */
@Immutable
data class HadithItem(
    val id: String,
    val collectionId: String,
    val title: String
)

/** Full bilingual text for the detail screen. */
@Immutable
data class HadithDetail(
    val id: String,
    val title: String,
    /** Arabic matn with tashkeel (always present). */
    val arabic: String,
    /** English translation; null when untranslated (API returns blank). */
    val english: String?,
    /** Attribution or grade, e.g. "Sahih al-Bukhari" / "Sahih". */
    val source: String
)

// --- hadeethenc.com wire DTOs (proven against the legacy client) ---

@Serializable
data class CategoryDto(
    val id: String,
    val title: String,
    val hadeeths_count: String,
    val parent_id: String? = null
)

@Serializable
data class HadithListItemDto(
    val id: String,
    val title: String
)

@Serializable
data class HadithListMeta(
    val current_page: String,
    val last_page: Int,
    val total_items: Int,
    val per_page: String
)

@Serializable
data class HadithListResponse(
    val data: List<HadithListItemDto>,
    val meta: HadithListMeta
)

@Serializable
data class HadithOneDto(
    val id: String,
    val title: String,
    val hadeeth: String,
    val attribution: String? = null,
    val grade: String? = null
)

/**
 * Static fallback catalog (ported from the legacy client). Used to seed Room
 * and as the offline fallback when the categories endpoint is unreachable.
 * Content data, not UI text — hence plain data, not string resources.
 */
val STATIC_HADITH_COLLECTIONS = listOf(
    HadithCollection("1", "صحيح البخاري", 7563, "الكتب التسعة"),
    HadithCollection("2", "صحيح مسلم", 7563, "الكتب التسعة"),
    HadithCollection("3", "سنن أبي داود", 5274, "الكتب التسعة"),
    HadithCollection("4", "سنن الترمذي", 3956, "الكتب التسعة"),
    HadithCollection("5", "سنن النسائي", 5758, "الكتب التسعة"),
    HadithCollection("6", "سنن ابن ماجه", 4341, "الكتب التسعة"),
    HadithCollection("7", "موطأ مالك", 1850, "الكتب التسعة"),
    HadithCollection("8", "مسند أحمد", 27647, "الكتب التسعة"),
    HadithCollection("9", "سنن الدارمي", 3503, "الكتب التسعة"),
    HadithCollection("10", "رياض الصالحين", 1900, "مجاميع معتمدة"),
    HadithCollection("11", "الأربعون النووية", 42, "مجاميع معتمدة"),
    HadithCollection("12", "بلوغ المرام", 1400, "مجاميع معتمدة"),
    HadithCollection("13", "عمدة الأحكام", 430, "مجاميع معتمدة"),
    HadithCollection("14", "الأدب المفرد", 1322, "مجاميع معتمدة"),
    HadithCollection("15", "الشمائل المحمدية", 415, "مجاميع معتمدة"),
    HadithCollection("16", "الترغيب والترهيب", 1300, "مجاميع معتمدة"),
    HadithCollection("17", "صحيح الجامع", 3600, "مجاميع معتمدة")
)
