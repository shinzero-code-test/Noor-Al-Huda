package com.exapps.nooralhuda.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorCard
import com.exapps.nooralhuda.feature.quran.domain.Surah

/** Greeting + Hijri date, resume-reading card, quick-action grid. Prayer hero lands in A4. */
@Composable
fun HomeScreen(
    onOpenQuran: () -> Unit,
    onOpenPrayer: () -> Unit,
    onOpenAzkar: () -> Unit,
    onOpenRadio: () -> Unit,
    onOpenSurah: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        NoorCard {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(state.greetingRes),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = state.hijriDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        state.resumeSurah?.let { surah ->
            ResumeCard(surah = surah, onClick = { onOpenSurah(surah.id) })
        }
        Text(
            text = stringResource(R.string.home_quick),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        QuickGrid(
            onOpenQuran = onOpenQuran,
            onOpenPrayer = onOpenPrayer,
            onOpenAzkar = onOpenAzkar,
            onOpenRadio = onOpenRadio
        )
    }
}

@Composable
private fun ResumeCard(surah: Surah, onClick: () -> Unit) {
    NoorCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.quran_resume),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = surah.arabic,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = surah.transliteration,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

private data class QuickAction(val labelRes: Int, val icon: ImageVector, val onClick: () -> Unit)

@Composable
private fun QuickGrid(
    onOpenQuran: () -> Unit,
    onOpenPrayer: () -> Unit,
    onOpenAzkar: () -> Unit,
    onOpenRadio: () -> Unit
) {
    val actions = listOf(
        Triple(R.string.tab_quran, Icons.Filled.MenuBook, onOpenQuran),
        Triple(R.string.tab_prayer, Icons.Filled.Mosque, onOpenPrayer),
        Triple(R.string.tab_azkar, Icons.Filled.SelfImprovement, onOpenAzkar),
        Triple(R.string.tab_radio, Icons.Filled.Radio, onOpenRadio)
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            QuickTile(actions[0], Modifier.weight(1f))
            QuickTile(actions[1], Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            QuickTile(actions[2], Modifier.weight(1f))
            QuickTile(actions[3], Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickTile(
    action: Triple<Int, ImageVector, () -> Unit>,
    modifier: Modifier = Modifier
) {
    NoorCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = action.third)
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                action.second,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = stringResource(action.first),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}
