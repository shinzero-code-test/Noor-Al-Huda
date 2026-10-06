package com.exapps.nooralhuda.feature.khatm.ui

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorCard
import com.exapps.nooralhuda.core.ui.components.NoorGhostButton
import com.exapps.nooralhuda.core.ui.components.NoorPrimaryButton
import com.exapps.nooralhuda.feature.khatm.domain.KhatmGroup

/**
 * Group khatm: create, join by code, my groups, open discovery.
 */
@Composable
fun KhatmScreen(
    onOpenGroup: (KhatmGroup) -> Unit,
    viewModel: KhatmListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.khatm_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item {
            NoorCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.groupName,
                        onValueChange = viewModel::onNameChange,
                        label = { Text(stringResource(R.string.khatm_group_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = state.openJoin,
                            onCheckedChange = viewModel::onOpenJoinChange
                        )
                        Text(
                            text = stringResource(R.string.khatm_open_join),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    NoorPrimaryButton(
                        onClick = viewModel::createGroup,
                        label = stringResource(R.string.khatm_create),
                        enabled = !state.busy,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        item {
            NoorCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.inviteCode,
                        onValueChange = viewModel::onCodeChange,
                        label = { Text(stringResource(R.string.khatm_invite_code)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    NoorGhostButton(
                        onClick = { viewModel.joinWithCode(onOpenGroup) },
                        label = stringResource(R.string.khatm_join),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        state.errorRes?.let { res ->
            item {
                Text(
                    text = stringResource(res),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        state.infoRes?.let { res ->
            item {
                Text(
                    text = stringResource(res),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (state.mine.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.khatm_mine),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            items(state.mine, key = { it.id }) { group ->
                GroupRow(group = group, onOpen = { onOpenGroup(group) })
            }
        }
        if (state.open.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.khatm_open),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            items(state.open, key = { it.id }) { group ->
                val joined = state.mine.any { it.id == group.id }
                GroupRow(
                    group = group,
                    actionLabel = if (joined) null else stringResource(R.string.khatm_join),
                    onOpen = { onOpenGroup(group) },
                    onAction = if (joined) null else ({ viewModel.joinOpen(group) })
                )
            }
        }
    }
}

@Composable
private fun GroupRow(
    group: KhatmGroup,
    onOpen: () -> Unit,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    NoorCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (actionLabel != null && onAction != null) {
                NoorGhostButton(onClick = onAction, label = actionLabel)
            }
        }
    }
}
