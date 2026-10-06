package com.exapps.nooralhuda.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
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
    onOpenHadith: () -> Unit,
    onOpenDua: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSeerah: () -> Unit,
    onOpenKnowledge: () -> Unit,
    onOpenRamadan: () -> Unit,
    onOpenKhatm: () -> Unit,
    onOpenSurah: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
        state.daily?.let { daily ->
            DailyCard(
                daily = daily,
                onOpenVerse = { onOpenSurah(daily.surahId) }
            )
        }
        TasbihCard(
            count = state.tasbihCount,
            today = state.tasbihToday,
            target = state.tasbihTarget,
            onTap = viewModel::tasbihTap,
            onReset = viewModel::tasbihReset
        )
        Text(
            text = stringResource(R.string.home_quick),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        QuickGrid(
            onOpenQuran = onOpenQuran,
            onOpenPrayer = onOpenPrayer,
            onOpenAzkar = onOpenAzkar,
            onOpenRadio = onOpenRadio,
            onOpenHadith = onOpenHadith,
            onOpenDua = onOpenDua,
            onOpenCalendar = onOpenCalendar,
            onOpenSeerah = onOpenSeerah,
            onOpenKnowledge = onOpenKnowledge,
            onOpenRamadan = onOpenRamadan,
            onOpenKhatm = onOpenKhatm
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
    onOpenRadio: () -> Unit,
    onOpenHadith: () -> Unit,
    onOpenDua: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSeerah: () -> Unit,
    onOpenKnowledge: () -> Unit,
    onOpenRamadan: () -> Unit,
    onOpenKhatm: () -> Unit
) {
    val actions = listOf(
        Triple(R.string.tab_quran, Icons.Filled.MenuBook, onOpenQuran),
        Triple(R.string.tab_prayer, Icons.Filled.Mosque, onOpenPrayer),
        Triple(R.string.tab_azkar, Icons.Filled.SelfImprovement, onOpenAzkar),
        Triple(R.string.tab_radio, Icons.Filled.Radio, onOpenRadio),
        Triple(R.string.home_hadith, Icons.Filled.AutoStories, onOpenHadith),
        Triple(R.string.home_dua, Icons.Filled.Favorite, onOpenDua),
        Triple(R.string.home_calendar, Icons.Filled.DateRange, onOpenCalendar),
        Triple(R.string.home_seerah, Icons.Filled.History, onOpenSeerah),
        Triple(R.string.home_knowledge, Icons.Filled.School, onOpenKnowledge),
        Triple(R.string.home_ramadan, Icons.Filled.NightsStay, onOpenRamadan),
        Triple(R.string.home_khatm, Icons.Filled.Groups, onOpenKhatm)
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        actions.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { QuickTile(it, Modifier.weight(1f)) }
                // Keep incomplete rows aligned with the 2-column grid.
                repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
            }
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

@Composable
private fun DailyCard(
    daily: com.exapps.nooralhuda.feature.daily.domain.DailyContent,
    onOpenVerse: () -> Unit
) {
    NoorCard {
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.home_verse_day),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = daily.verseArabic,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${daily.verseRef} — ${daily.verseTranslation}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onOpenVerse) {
                Text(stringResource(R.string.home_open_verse))
            }
            Text(
                text = stringResource(R.string.home_hadith_day),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = daily.hadithText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = daily.hadithSource,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TasbihCard(
    count: Int,
    today: Int,
    target: Int,
    onTap: () -> Unit,
    onReset: () -> Unit
) {
    NoorCard {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.home_tasbih),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$count / $target",
                style = MaterialTheme.typography.displaySmall,
                color = if (count >= target) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.home_tasbih_today, today),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onTap,
                modifier = Modifier.size(120.dp),
                shape = CircleShape
            ) {
                Text(stringResource(R.string.home_tasbih_tap))
            }
            TextButton(onClick = onReset) {
                Text(stringResource(R.string.home_tasbih_reset))
            }
        }
    }
}
