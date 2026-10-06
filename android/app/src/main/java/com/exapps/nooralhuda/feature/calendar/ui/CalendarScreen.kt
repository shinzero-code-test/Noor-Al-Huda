package com.exapps.nooralhuda.feature.calendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.datetime.mondayFirstIndex
import com.exapps.nooralhuda.core.ui.components.NoorCard
import com.exapps.nooralhuda.feature.calendar.domain.IslamicEvent
import java.time.DayOfWeek
import java.time.format.TextStyle

/**
 * Islamic calendar: Hijri month grid (Gregorian-mapped cells, Jumuah +
 * White Days + occasion markers, today ring) plus the occasions list.
 * Transcribed from the approved Islamic Calendar mockup.
 */
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val month = state.month

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.shiftMonth(-1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.cal_prev))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.cal_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = viewModel.monthTitle(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { viewModel.shiftMonth(1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.cal_next))
                }
            }
        }
        if (month != null) {
            item {
                NoorCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        WeekdayHeader(state.weekdayNames)
                        val leadBlanks = month.days.firstOrNull()
                            ?.gregorian?.dayOfWeek?.mondayFirstIndex() ?: 0
                        val cells: List<Int?> =
                            List(leadBlanks) { null } + month.days.map { it.hijriDay }
                        val rows = (cells.size + 6) / 7
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((rows * 52).dp)
                                .padding(top = 4.dp),
                            userScrollEnabled = false
                        ) {
                            items(cells) { hijriDay ->
                                DayCell(
                                    hijriDay = hijriDay,
                                    gregorianDay = hijriDay?.let { h ->
                                        month.days.firstOrNull { it.hijriDay == h }?.gregorian?.dayOfMonth
                                    },
                                    isToday = hijriDay != null &&
                                        month.days.firstOrNull { it.hijriDay == hijriDay }
                                            ?.gregorian == state.today,
                                    isFriday = hijriDay != null &&
                                        month.days.firstOrNull { it.hijriDay == hijriDay }
                                            ?.gregorian?.dayOfWeek == DayOfWeek.FRIDAY,
                                    isWhiteDay = hijriDay in 13..15,
                                    hasEvent = hijriDay != null && state.events.any {
                                        it.hijriMonth == month.hijriMonth && it.hijriDay == hijriDay
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.cal_events),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        items(state.events, key = { it.id }) { event ->
            EventRow(event = event, onToggle = { viewModel.toggleReminder(event) })
        }
    }
}

@Composable
private fun WeekdayHeader(names: List<String>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Monday-first to match the grid offset math.
        val fridayIndex = 4
        names.forEachIndexed { index, name ->
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                color = if (index == fridayIndex) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DayCell(
    hijriDay: Int?,
    gregorianDay: Int?,
    isToday: Boolean,
    isFriday: Boolean,
    isWhiteDay: Boolean,
    hasEvent: Boolean
) {
    Column(
        modifier = Modifier.aspectRatio(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (hijriDay == null) return
        Text(
            text = gregorianDay.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                isToday -> MaterialTheme.colorScheme.primary
                isFriday -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        Text(
            text = buildString {
                append(hijriDay)
                if (isWhiteDay) append(" ◌")
                if (hasEvent) append(" •")
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EventRow(event: IslamicEvent, onToggle: () -> Unit) {
    NoorCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${event.hijriDay} / ${event.hijriMonth}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (event.reminder) Icons.Filled.Notifications
                    else Icons.Filled.NotificationsOff,
                    contentDescription = stringResource(R.string.cal_remind)
                )
            }
        }
    }
}
