package com.exapps.nooralhuda.feature.azkar

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorComingSoon

@Composable
fun AzkarScreen() {
    NoorComingSoon(title = stringResource(R.string.azkar_title))
}
