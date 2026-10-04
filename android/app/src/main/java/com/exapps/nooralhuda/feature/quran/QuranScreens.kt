package com.exapps.nooralhuda.feature.quran

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorComingSoon

@Composable
fun QuranScreen(onSurahClick: (Int) -> Unit) {
    NoorComingSoon(title = stringResource(R.string.quran_title))
}

@Composable
fun SurahDetailScreen(surahId: Int) {
    NoorComingSoon(title = stringResource(R.string.quran_title) + " $surahId")
}
