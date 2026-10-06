package com.exapps.nooralhuda.feature.calendar.domain

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.StateFlow

/** A fixed-date Islamic occasion (Hijri month/day, recurs yearly). */
@Immutable
data class IslamicEvent(
    val id: String,
    val title: String,
    val hijriMonth: Int,
    val hijriDay: Int,
    val description: String,
    val reminder: Boolean = false
)

interface CalendarRepository {
    val events: StateFlow<List<IslamicEvent>>
    /** Seed Room from the static list on first run; reload afterwards. */
    suspend fun warm()
    suspend fun toggleReminder(id: String)
}

/**
 * Static occasions (ported from the legacy client). Content data,
 * not UI text — hence plain data, not string resources.
 */
val STATIC_ISLAMIC_EVENTS = listOf(
    IslamicEvent("ramadan", "بداية رمضان", 9, 1, "شهر الصيام والقرآن والتزكية."),
    IslamicEvent("eid-fitr", "عيد الفطر", 10, 1, "فرحة إتمام الصيام وشكر النعمة."),
    IslamicEvent("hajj-days", "أيام الحج", 12, 8, "بداية المناسك وأيام التلبية والوقوف بعرفة."),
    IslamicEvent("eid-adha", "عيد الأضحى", 12, 10, "عيد التضحية والطاعة والرحمة."),
    IslamicEvent("muharram", "عاشوراء", 1, 10, "يوم نجّى الله فيه موسى عليه السلام."),
    IslamicEvent("mawlid", "ذكرى المولد النبوي", 3, 12, "محطة للتذكير بالسيرة والاقتداء بالأخلاق النبوية.")
)
