package com.exapps.nooralhuda.feature.radio

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorComingSoon

/** Full Media3 radio lands in v1.5. */
@Composable
fun RadioScreen() {
    NoorComingSoon(title = stringResource(R.string.radio_title))
}
