package com.exapps.nooralhuda.feature.khatm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.exapps.nooralhuda.core.ui.components.NoorError
import com.exapps.nooralhuda.core.ui.components.NoorGhostButton
import com.exapps.nooralhuda.core.ui.components.NoorPrimaryButton
import com.exapps.nooralhuda.feature.khatm.domain.KhatmMember

/**
 * Group detail: union progress, members, next-page claim, invite sharing,
 * leave / creator-remove.
 */
@Composable
fun KhatmDetailScreen(
    onGone: () -> Unit,
    viewModel: KhatmDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = state.groupName.ifBlank { stringResource(R.string.khatm_title) },
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
                    Text(
                        text = stringResource(R.string.khatm_progress, state.donePages, state.percent),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LinearProgressIndicator(
                        progress = { state.percent / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    state.nextPage?.let { page ->
                        NoorPrimaryButton(
                            onClick = viewModel::markNextDone,
                            label = stringResource(R.string.khatm_mark_page, page),
                            enabled = !state.busy,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } ?: Text(
                        text = stringResource(R.string.khatm_complete),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        state.inviteCode?.let { code ->
            item {
                NoorCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.khatm_invite_code),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = code,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { viewModel.shareCode(code, context) }) {
                            Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.khatm_share))
                        }
                    }
                }
            }
        }
        state.errorRes?.let { res ->
            item {
                NoorError(
                    message = stringResource(res),
                    onRetry = viewModel::markNextDone
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.khatm_members, state.members.size),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        items(state.members, key = { it.uid }) { member ->
            MemberRow(
                member = member,
                canRemove = state.isCreator,
                onRemove = { viewModel.removeMember(member) }
            )
        }
        item {
            NoorGhostButton(
                onClick = { viewModel.leave(onGone) },
                label = stringResource(R.string.khatm_leave),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MemberRow(
    member: KhatmMember,
    canRemove: Boolean,
    onRemove: () -> Unit
) {
    NoorCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = member.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.khatm_member_pages, member.completedPages.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (canRemove) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Filled.PersonRemove,
                        contentDescription = stringResource(R.string.khatm_remove)
                    )
                }
            }
        }
    }
}
