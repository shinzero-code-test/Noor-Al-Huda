package com.exapps.nooralhuda.feature.hadith

import androidx.compose.runtime.Composable
import com.exapps.nooralhuda.core.ui.components.NoorComingSoon

@Composable
fun HadithDetailScreen(hadithId: String) {
    NoorComingSoon(title = hadithId)
}
