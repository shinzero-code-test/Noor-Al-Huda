package com.exapps.nooralhuda.feature.seerah.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorCard
import com.exapps.nooralhuda.feature.seerah.domain.SeerahChapter

/**
 * Seerah chapters: list + long-form reader with TTS and share.
 * Transcribed from the approved Seerah reading mockup.
 */
@Composable
fun SeerahListScreen(
    onOpenChapter: (String) -> Unit,
    viewModel: SeerahListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.seerah_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(state.chapters, key = { it.id }) { chapter ->
                NoorCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenChapter(chapter.id) }
                ) {
                    Column(
                        Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = chapter.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = chapter.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = chapter.lessons.joinToString(" • "),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeerahReaderScreen(
    viewModel: SeerahReaderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val chapter = state.chapter

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (chapter != null) {
            ReaderBody(
                chapter = chapter,
                onSpeak = viewModel::speak,
                onShare = { shareChapter(chapter, context) }
            )
        }
    }
}

@Composable
private fun ReaderBody(
    chapter: SeerahChapter,
    onSpeak: () -> Unit,
    onShare: () -> Unit
) {
    NoorCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = chapter.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            androidx.compose.runtime.CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {
                Text(
                    text = chapter.summary,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.8
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = chapter.reflection,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.8
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text(
                text = chapter.lessons.joinToString(" • "),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = onSpeak) {
            Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.seerah_listen))
        }
        IconButton(onClick = onShare) {
            Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.seerah_share))
        }
    }
}

private fun shareChapter(chapter: SeerahChapter, context: Context) {
    val text = "${chapter.title}\n\n${chapter.summary}\n\n${chapter.reflection}"
    val send = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, text)
    context.startActivity(
        Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
