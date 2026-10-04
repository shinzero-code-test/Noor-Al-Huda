package com.exapps.nooralhuda.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorComingSoon

@Composable
fun SettingsScreen() {
    NoorComingSoon(title = stringResource(R.string.settings_title))
}
