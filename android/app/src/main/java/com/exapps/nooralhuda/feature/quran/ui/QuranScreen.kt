package com.exapps.nooralhuda.feature.quran.ui

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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
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
import com.exapps.nooralhuda.feature.quran.domain.Surah

@Composable
fun QuranScreen(
    onSurahClick: (Int) -> Unit,
    viewModel: QuranListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when {
        state.loading -> NoorLoading()
        state.error != null && state.surahs.isEmpty() ->
            NoorError(message = state.error ?: "", onRetry = { viewModel.refresh() })
        else -> QuranList(
            state = state,
            onQuery = viewModel::onQueryChange,
            onSurahClick = {
                viewModel.setLastRead(it.id)
                onSurahClick(it.id)
            }
        )
    }
}

@Composable
private fun QuranList(
    state: QuranListUiState,
    onQuery: (String) -> Unit,
    onSurahClick: (Surah) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.quran_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQuery,
                label = { Text(stringResource(R.string.quran_search)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        state.lastReadSurahId?.let { lastId ->
            state.surahs.firstOrNull { it.id == lastId }?.let { last ->
                item {
                    NoorCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSurahClick(last) }
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.quran_resume),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = last.arabic,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                Icons.Filled.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        items(state.surahs, key = { it.id }) { surah ->
            NoorCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSurahClick(surah) }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = surah.arabic,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${surah.transliteration} • ${surah.verses}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Filled.BookmarkBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
