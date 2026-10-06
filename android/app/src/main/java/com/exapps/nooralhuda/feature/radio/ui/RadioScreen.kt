package com.exapps.nooralhuda.feature.radio.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorCard
import com.exapps.nooralhuda.core.ui.components.NoorError
import com.exapps.nooralhuda.feature.radio.domain.RadioStation

private val SLEEP_OPTIONS = listOf(null, 15, 30, 60)

/**
 * Radio full player: now-playing hero, sleep timer, station directory.
 * Transcribed from the approved Radio Full Player mockup. Live streams
 * cannot seek, so the mockup's ±30s buttons are intentionally omitted.
 */
@Composable
fun RadioScreen(
    viewModel: RadioViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val selected = state.stations.firstOrNull { it.id == state.selectedId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.radio_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )
        if (selected != null) {
            NowPlayingHero(
                station = selected,
                isPlaying = state.isPlaying,
                connected = state.connected,
                sleepRemaining = state.sleepRemaining,
                onToggle = viewModel::toggle,
                onFavourite = { viewModel.toggleFavourite(selected) },
                onShare = { viewModel.shareStation(selected, context) }
            )
        } else {
            CircularProgressIndicator(modifier = Modifier.padding(24.dp))
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(SLEEP_OPTIONS) { minutes ->
                FilterChip(
                    selected = state.sleepMinutes == minutes,
                    onClick = { viewModel.setSleepTimer(minutes) },
                    label = {
                        Text(
                            if (minutes == null) stringResource(R.string.radio_sleep_off)
                            else stringResource(R.string.radio_sleep_minutes, minutes)
                        )
                    }
                )
            }
        }
        if (state.stations.isEmpty()) {
            NoorError(
                message = stringResource(R.string.radio_error_load),
                onRetry = viewModel::retry
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(state.stations, key = { it.id }) { station ->
                    val playing = station.id == state.selectedId && state.isPlaying
                    NoorCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.select(station) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (playing) Icons.Filled.Pause
                                else Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = station.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (playing) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                if (playing) {
                                    Text(
                                        text = stringResource(R.string.radio_on_air),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.toggleFavourite(station) }) {
                                Icon(
                                    imageVector = if (station.favourite) Icons.Filled.Favorite
                                    else Icons.Filled.FavoriteBorder,
                                    contentDescription = stringResource(R.string.radio_favourite)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NowPlayingHero(
    station: RadioStation,
    isPlaying: Boolean,
    connected: Boolean,
    sleepRemaining: Int?,
    onToggle: () -> Unit,
    onFavourite: () -> Unit,
    onShare: () -> Unit
) {
    NoorCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = station.name.take(1),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = station.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = when {
                    !connected -> stringResource(R.string.radio_connecting)
                    isPlaying -> stringResource(R.string.radio_on_air)
                    else -> stringResource(R.string.radio_paused)
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            sleepRemaining?.let {
                Text(
                    text = stringResource(R.string.radio_sleep_remaining, it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onToggle, enabled = connected) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause
                        else Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.radio_toggle),
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onFavourite) {
                    Icon(
                        imageVector = if (station.favourite) Icons.Filled.Favorite
                        else Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(R.string.radio_favourite)
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = stringResource(R.string.radio_share)
                    )
                }
            }
        }
    }
}
