package com.exapps.nooralhuda.feature.calendar.data

import com.exapps.nooralhuda.feature.calendar.domain.CalendarRepository
import com.exapps.nooralhuda.feature.calendar.domain.IslamicEvent
import com.exapps.nooralhuda.feature.calendar.domain.STATIC_ISLAMIC_EVENTS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fully offline occasions catalog. The static list seeds Room on first run;
 * reminder toggles persist locally. No network involved at all.
 */
@Singleton
class RoomCalendarRepository @Inject constructor(
    private val dao: CalendarEventDao
) : CalendarRepository {

    private val _events = MutableStateFlow<List<IslamicEvent>>(STATIC_ISLAMIC_EVENTS)
    override val events: StateFlow<List<IslamicEvent>> = _events.asStateFlow()

    override suspend fun warm() {
        if (dao.count() == 0) {
            dao.insertAll(
                STATIC_ISLAMIC_EVENTS.map {
                    CalendarEventEntity(it.id, it.title, it.hijriMonth, it.hijriDay, it.description)
                }
            )
        }
        reload()
    }

    override suspend fun toggleReminder(id: String) {
        dao.toggleReminder(id)
        reload()
    }

    private suspend fun reload() {
        _events.value = dao.all().map {
            IslamicEvent(it.id, it.title, it.hijriMonth, it.hijriDay, it.description, it.reminder)
        }
    }
}
