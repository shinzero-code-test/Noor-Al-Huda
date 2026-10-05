package com.exapps.nooralhuda.feature.quran.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorCard
import com.exapps.nooralhuda.core.ui.components.NoorError
import com.exapps.nooralhuda.core.ui.components.NoorLoading
import com.exapps.nooralhuda.core.ui.theme.QuranVerseStyle
import com.exapps.nooralhuda.feature.quran.domain.TajweedParser
import com.exapps.nooralhuda.feature.quran.domain.Verse

@Composable
fun SurahDetailScreen(viewModel: SurahDetailViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when {
        state.loading -> NoorLoading()
        state.error != null && state.verses.isEmpty() ->
            NoorError(message = state.error ?: "", onRetry = viewModel::load)
        else -> ReaderContent(state = state, viewModel = viewModel)
    }
}

@Composable
private fun ReaderContent(state: SurahDetailUiState, viewModel: SurahDetailViewModel) {
    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.showTranslation,
                        onClick = viewModel::toggleTranslation,
                        label = { Text(stringResource(R.string.reader_translation)) }
                    )
                    FilterChip(
                        selected = state.showTajweed,
                        onClick = viewModel::toggleTajweed,
                        label = { Text(stringResource(R.string.reader_tajweed)) }
                    )
                }
            }
            if (state.showTajweed) {
                item { TajweedLegend() }
            }
            items(state.verses, key = { it.number }) { verse ->
                VerseRow(
                    verse = verse,
                    showTranslation = state.showTranslation,
                    showTajweed = state.showTajweed,
                    bookmarked = state.bookmarkedKeys.contains("${verse.surahId}:${verse.number}"),
                    onBookmark = { viewModel.toggleBookmark(verse) }
                )
            }
        }
        val audioLabel = stringResource(R.string.reader_audio_label, state.surahId)
        AudioBar(
            label = state.audioLabel,
            isPlaying = state.isPlaying,
            downloaded = state.downloaded,
            onPlay = { viewModel.play(audioLabel) },
            onToggle = viewModel::togglePlay,
            onDownload = viewModel::download,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun TajweedLegend() {
    val labels = listOf(
        R.string.tajweed_ghunnah to TajweedParser.legend[0].second,
        R.string.tajweed_madd to TajweedParser.legend[1].second,
        R.string.tajweed_qalqalah to TajweedParser.legend[2].second
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        labels.forEach { (res, color) ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(color = color)
                }
                Text(
                    text = stringResource(res),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VerseRow(
    verse: Verse,
    showTranslation: Boolean,
    showTajweed: Boolean,
    bookmarked: Boolean,
    onBookmark: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val text = if (showTajweed) {
        TajweedParser.toAnnotatedString(
            tajweed = verse.tajweed,
            plain = verse.arabic,
            baseColor = colors.onSurface,
            markerColor = colors.primary
        )
    } else {
        androidx.compose.ui.text.AnnotatedString(verse.arabic)
    }
    NoorCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = verse.number.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onBookmark) {
                    Icon(
                        if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = null,
                        tint = if (bookmarked) colors.primary else colors.onSurfaceVariant
                    )
                }
            }
            Text(
                text = text,
                style = QuranVerseStyle.copy(textDirection = TextDirection.Rtl),
                color = colors.onSurface
            )
            if (showTranslation && verse.translation.isNotBlank()) {
                Text(
                    text = TajweedParser.cleanTranslation(verse.translation),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDirection = TextDirection.Ltr
                    ),
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AudioBar(
    label: String?,
    isPlaying: Boolean,
    downloaded: Boolean,
    onPlay: () -> Unit,
    onToggle: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    NoorCard(modifier = modifier) {
        Row(Modifier.fillMaxWidth().padding(16.dp)) {
            IconButton(onClick = { if (label == null) onPlay() else onToggle() }) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = label ?: stringResource(R.string.reader_play),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            IconButton(onClick = onDownload, enabled = !downloaded) {
                Icon(
                    if (downloaded) Icons.Filled.Done else Icons.Filled.Download,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
