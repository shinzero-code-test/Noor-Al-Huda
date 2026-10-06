package com.exapps.nooralhuda.feature.hadith.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorCard
import com.exapps.nooralhuda.core.ui.components.NoorError

/**
 * Hadith library: collection chips + search over loaded items + paged list.
 * Transcribed from the approved Hadith Library mockup.
 */
@Composable
fun HadithListScreen(
    onOpenDetail: (String) -> Unit,
    viewModel: HadithListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val paging = viewModel.items.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.hadith_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            label = { Text(stringResource(R.string.hadith_search)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(state.collections, key = { it.id }) { collection ->
                FilterChip(
                    selected = collection.id == state.selectedId,
                    onClick = { viewModel.selectCollection(collection.id) },
                    label = { Text(collection.title) }
                )
            }
        }
        val refresh = paging.loadState.refresh
        val append = paging.loadState.append
        when {
            refresh is LoadState.Loading && paging.itemCount == 0 -> {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            }
            refresh is LoadState.Error && paging.itemCount == 0 -> {
                // Offline with empty cache: cached collections still show above.
                NoorError(
                    message = stringResource(R.string.hadith_error_load),
                    onRetry = { paging.retry() }
                )
            }
            else -> {
                val needle = state.query.trim()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    val snapshot = paging.itemSnapshotList.items
                    val visible = if (needle.isEmpty()) snapshot
                    else snapshot.filter {
                        it.title.contains(needle, ignoreCase = true)
                    }
                    if (visible.isEmpty() && append.endOfPaginationReached) {
                        item {
                            Text(
                                text = stringResource(R.string.hadith_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            )
                        }
                    }
                    items(visible, key = { "${it.collectionId}:${it.id}" }) { item ->
                        NoorCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenDetail(item.id) }
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    if (append is LoadState.Loading) {
                        item {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                        }
                    }
                    if (append is LoadState.Error) {
                        item {
                            NoorError(
                                message = stringResource(R.string.hadith_error_page),
                                onRetry = { paging.retry() }
                            )
                        }
                    }
                }
            }
        }
    }
}
