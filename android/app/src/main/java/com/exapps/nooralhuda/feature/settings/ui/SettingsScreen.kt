package com.exapps.nooralhuda.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorCard
import com.exapps.nooralhuda.core.ui.components.NoorGhostButton
import com.exapps.nooralhuda.core.ui.components.NoorLoading

/** Every preference row is a real DataStore read/write. Nothing decorative. */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (state.loading) {
        NoorLoading()
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        item {
            NoorCard {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = state.email ?: stringResource(R.string.settings_guest),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (state.signedIn) stringResource(R.string.settings_signed_in)
                        else stringResource(R.string.settings_signed_out),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            SettingsSection(titleRes = R.string.settings_prayer) {
                MethodRow(selected = state.calcMethod, onSelect = viewModel::setCalcMethod)
                ToggleRow(
                    label = stringResource(R.string.settings_notifications),
                    checked = state.notifications,
                    onChange = viewModel::setNotifications
                )
                ToggleRow(
                    label = stringResource(R.string.settings_hourly),
                    checked = state.hourlyDhikr,
                    onChange = viewModel::setHourlyDhikr
                )
            }
        }
        item {
            SettingsSection(titleRes = R.string.settings_quran) {
                MethodRow(
                    selected = state.fontFamily,
                    options = listOf("naskh" to R.string.settings_font_naskh, "amiri" to R.string.settings_font_amiri),
                    onSelect = viewModel::setFontFamily
                )
                Text(
                    text = stringResource(R.string.settings_font_scale),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = state.fontScale,
                    onValueChange = viewModel::setFontScale,
                    valueRange = 0.8f..1.6f
                )
            }
        }
        item {
            SettingsSection(titleRes = R.string.settings_data) {
                NoorGhostButton(
                    onClick = viewModel::wipe,
                    label = stringResource(R.string.settings_wipe),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(titleRes: Int, content: @Composable () -> Unit) {
    NoorCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun MethodRow(
    selected: String,
    onSelect: (String) -> Unit,
    options: List<Pair<String, Int>> = listOf(
        "ummAlQura" to R.string.method_umm,
        "egyptian" to R.string.method_egyptian,
        "karachi" to R.string.method_karachi
    )
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (value, label) ->
            androidx.compose.material3.FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(stringResource(label)) }
            )
        }
    }
}
