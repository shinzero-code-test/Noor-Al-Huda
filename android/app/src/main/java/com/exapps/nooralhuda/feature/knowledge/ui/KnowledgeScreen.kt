package com.exapps.nooralhuda.feature.knowledge.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorCard
import com.exapps.nooralhuda.feature.knowledge.domain.KnowledgeEntry
import com.exapps.nooralhuda.feature.knowledge.domain.KnowledgeKind

/**
 * Knowledge hub: search, section grid, name of the day, FAQ accordion,
 * ebooks and streams. Transcribed from the approved Knowledge Hub mockup.
 */
@Composable
fun KnowledgeScreen(
    viewModel: KnowledgeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.knowledge_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            label = { Text(stringResource(R.string.knowledge_search)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        val sections = listOf(
            null to stringResource(R.string.knowledge_all),
            KnowledgeKind.NAME to stringResource(R.string.knowledge_names),
            KnowledgeKind.RUQYAH to stringResource(R.string.knowledge_ruqyah),
            KnowledgeKind.FAQ to stringResource(R.string.knowledge_faq),
            KnowledgeKind.EBOOK to stringResource(R.string.knowledge_ebooks),
            KnowledgeKind.STREAM to stringResource(R.string.knowledge_streams)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false
        ) {
            items(sections) { (kind, label) ->
                FilterChip(
                    selected = state.section == kind,
                    onClick = { viewModel.selectSection(kind) },
                    label = { Text(label, maxLines = 1) }
                )
            }
        }
        val needle = state.query.trim()
        val visible = state.entries.filter { entry ->
            (state.section == null || entry.kind == state.section) &&
                (needle.isEmpty() ||
                    entry.title.contains(needle, ignoreCase = true) ||
                    entry.body.contains(needle, ignoreCase = true))
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (state.section == null && needle.isEmpty()) {
                state.nameOfTheDay?.let { name ->
                    item {
                        NoorCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.knowledge_name_of_day),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = name.title,
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${name.subtitle} — ${name.body}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(onClick = { viewModel.speak(name) }) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.knowledge_listen))
                                }
                            }
                        }
                    }
                }
            }
            items(visible, key = { it.id }) { entry ->
                when (entry.kind) {
                    KnowledgeKind.FAQ -> FaqRow(
                        entry = entry,
                        expanded = state.expandedFaq == entry.id,
                        onToggle = { viewModel.toggleFaq(entry.id) }
                    )
                    else -> EntryCard(
                        entry = entry,
                        onSpeak = { viewModel.speak(entry) },
                        onOpen = entry.url?.let { url -> { viewModel.openUrl(url, context) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun FaqRow(entry: KnowledgeEntry, expanded: Boolean, onToggle: () -> Unit) {
    NoorCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }
            if (expanded) {
                Text(
                    text = entry.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun EntryCard(entry: KnowledgeEntry, onSpeak: () -> Unit, onOpen: (() -> Unit)?) {
    NoorCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = entry.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row {
                IconButton(onClick = onSpeak) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.knowledge_listen))
                }
                if (onOpen != null) {
                    IconButton(onClick = onOpen) {
                        Icon(Icons.Filled.OpenInNew, contentDescription = stringResource(R.string.knowledge_open))
                    }
                }
            }
        }
    }
}
