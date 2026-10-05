package com.exapps.nooralhuda.feature.azkar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorCard
import com.exapps.nooralhuda.core.ui.components.NoorError
import com.exapps.nooralhuda.core.ui.components.NoorLoading
import com.exapps.nooralhuda.feature.azkar.ui.AzkarViewModel
import com.exapps.nooralhuda.feature.prayer.data.AzkarEntry

private val COLLECTIONS = listOf("morning", "evening", "after-prayer")

@Composable
fun AzkarScreen(viewModel: AzkarViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when {
        state.loading -> NoorLoading()
        state.error != null && state.entries.isEmpty() ->
            NoorError(message = state.error ?: "", onRetry = { viewModel.load(state.collection) })
        else -> AzkarList(viewModel = viewModel, state = state)
    }
}

@Composable
private fun AzkarList(
    viewModel: AzkarViewModel,
    state: com.exapps.nooralhuda.feature.azkar.ui.AzkarUiState
) {
    val visible = if (state.query.isBlank()) state.entries
    else state.entries.filter { it.text.contains(state.query) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.azkar_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                COLLECTIONS.forEach { collection ->
                    FilterChip(
                        selected = state.collection == collection,
                        onClick = { viewModel.load(collection) },
                        label = { Text(stringResource(collectionRes(collection))) }
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text(stringResource(R.string.azkar_search)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        items(visible, key = { it.id }) { entry ->
            val done = state.progress[entry.id] ?: 0
            AzkarCard(
                entry = entry,
                done = done,
                onCount = { viewModel.increment(entry) },
                onReset = { viewModel.reset(entry) },
                onSpeak = { viewModel.speak(entry) }
            )
        }
    }
}

private fun collectionRes(collection: String): Int = when (collection) {
    "evening" -> R.string.azkar_evening
    "after-prayer" -> R.string.azkar_after_prayer
    else -> R.string.azkar_morning
}

@Composable
private fun AzkarCard(
    entry: AzkarEntry,
    done: Int,
    onCount: () -> Unit,
    onReset: () -> Unit,
    onSpeak: () -> Unit
) {
    val complete = done >= entry.count
    NoorCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onCount)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = entry.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = entry.virtue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (entry.count > 1) {
                    CircularProgressIndicator(
                        progress = { done.toFloat() / entry.count },
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.azkar_progress, done, entry.count),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (complete) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onSpeak) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = null)
                    }
                    IconButton(onClick = onReset) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                    }
                }
            }
        }
    }
}
