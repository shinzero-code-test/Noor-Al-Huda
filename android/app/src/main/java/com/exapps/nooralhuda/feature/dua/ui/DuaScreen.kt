package com.exapps.nooralhuda.feature.dua.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorCard
import com.exapps.nooralhuda.feature.dua.domain.DuaCategory
import com.exapps.nooralhuda.feature.dua.domain.DuaEntry

/**
 * Dua catalog: search, category chips, counter cards, locked AI teaser.
 * Transcribed from the approved Dua Catalog mockup.
 */
@Composable
fun DuaScreen(
    viewModel: DuaViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.dua_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            label = { Text(stringResource(R.string.dua_search)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = state.category == null,
                    onClick = { viewModel.selectCategory(null) },
                    label = { Text(stringResource(R.string.dua_all)) }
                )
            }
            items(DuaCategory.entries) { category ->
                FilterChip(
                    selected = state.category == category,
                    onClick = { viewModel.selectCategory(category) },
                    label = { Text(category.titleEn) }
                )
            }
        }
        val needle = state.query.trim()
        val visible = state.duas.filter { dua ->
            (state.category == null || dua.category == state.category) &&
                (needle.isEmpty() ||
                    dua.arabic.contains(needle) ||
                    dua.translation.contains(needle, ignoreCase = true) ||
                    dua.transliteration.contains(needle, ignoreCase = true))
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(visible, key = { it.id }) { dua ->
                DuaCard(
                    dua = dua,
                    count = state.counts[dua.id] ?: 0,
                    onIncrement = { viewModel.increment(dua.id, dua.repeat) },
                    onReset = { viewModel.reset(dua.id) },
                    onSpeak = { viewModel.speak(dua) },
                    onFavourite = { viewModel.toggleFavourite(dua) }
                )
            }
            item {
                NoorCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.dua_ai_soon_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.dua_ai_soon_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DuaCard(
    dua: DuaEntry,
    count: Int,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onSpeak: () -> Unit,
    onFavourite: () -> Unit
) {
    NoorCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "×${dua.repeat}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onSpeak) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.dua_listen))
                }
                IconButton(onClick = onFavourite) {
                    Icon(
                        imageVector = if (dua.favourite) Icons.Filled.Favorite
                        else Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(R.string.dua_favourite)
                    )
                }
            }
            androidx.compose.runtime.CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {
                Text(
                    text = dua.arabic,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.8
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text(
                text = dua.transliteration,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = dua.translation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = dua.source,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onReset) {
                    Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.dua_reset))
                }
                Text(
                    text = "$count / ${dua.repeat}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onIncrement,
                    enabled = count < dua.repeat
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.dua_count))
                }
            }
        }
    }
}
