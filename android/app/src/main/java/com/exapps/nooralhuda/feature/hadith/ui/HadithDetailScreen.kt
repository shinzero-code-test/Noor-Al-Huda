package com.exapps.nooralhuda.feature.hadith.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
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
import com.exapps.nooralhuda.core.ui.components.NoorError
import com.exapps.nooralhuda.feature.hadith.domain.HadithDetail

/**
 * Hadith detail: full matn (RTL) + translation + source + actions.
 * Transcribed from the approved Hadith Detail mockup. Bookmarking arrives
 * with the 1.2.0 content pack (needs a bookmark schema extension).
 */
@Composable
fun HadithDetailScreen(
    viewModel: HadithDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            state.loading -> CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            state.detail != null -> DetailBody(
                detail = state.detail!!,
                onSpeak = viewModel::speak,
                onCopy = { viewModel.copyText(context) },
                onShare = { viewModel.shareText(context) },
                onReport = viewModel::report
            )
            else -> NoorError(
                message = stringResource(R.string.hadith_error_load),
                onRetry = viewModel::load
            )
        }
        state.errorRes?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        state.infoRes?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DetailBody(
    detail: HadithDetail,
    onSpeak: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit
) {
    NoorCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = detail.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            // Arabic matn is always RTL, regardless of UI locale.
            androidx.compose.runtime.CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {
                Text(
                    text = detail.arabic,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.8
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            detail.english?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = detail.source,
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
            Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.hadith_listen))
        }
        IconButton(onClick = onCopy) {
            Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.hadith_copy))
        }
        IconButton(onClick = onShare) {
            Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.hadith_share))
        }
        IconButton(onClick = onReport) {
            Icon(Icons.Filled.Flag, contentDescription = stringResource(R.string.hadith_report))
        }
    }
}
