package com.exapps.nooralhuda.feature.radio

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorComingSoon

@Composable
fun RadioScreen() {
    NoorComingSoon(title = stringResource(R.string.radio_title))
}
