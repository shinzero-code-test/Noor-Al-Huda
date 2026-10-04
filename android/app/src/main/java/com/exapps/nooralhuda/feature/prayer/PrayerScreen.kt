package com.exapps.nooralhuda.feature.prayer

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorComingSoon

@Composable
fun PrayerScreen() {
    NoorComingSoon(title = stringResource(R.string.prayer_title))
}
