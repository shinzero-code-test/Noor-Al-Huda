package com.exapps.nooralhuda.feature.prayer.ui

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorCard
import com.exapps.nooralhuda.core.ui.components.NoorError
import com.exapps.nooralhuda.core.ui.components.NoorGhostButton
import com.exapps.nooralhuda.core.ui.components.NoorLoading
import com.exapps.nooralhuda.feature.prayer.domain.PrayerName
import com.exapps.nooralhuda.feature.prayer.domain.PrayerTime
import java.util.concurrent.TimeUnit

@Composable
fun PrayerScreen(
    onOpenQibla: () -> Unit,
    onOpenTracker: () -> Unit,
    viewModel: PrayerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.load()
    }

    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(AlarmManager::class.java)
            viewModel.exactAlarmAllowed(manager?.canScheduleExactAlarms() != false)
        }
    }

    when {
        state.loading -> NoorLoading()
        state.error != null && state.day == null ->
            NoorError(message = state.error ?: "", onRetry = viewModel::load)
        state.day != null -> PrayerContent(
            state = state,
            viewModel = viewModel,
            onOpenQibla = onOpenQibla,
            onOpenTracker = onOpenTracker
        )
    }
}

@Composable
private fun PrayerContent(
    state: PrayerUiState,
    viewModel: PrayerViewModel,
    onOpenQibla: () -> Unit,
    onOpenTracker: () -> Unit
) {
    val day = state.day ?: return
    val next = day.next()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = day.locationLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (next != null) {
            item {
                NextPrayerHero(
                    next = next,
                    qibla = day.qiblaDegrees,
                    progress = dayProgress(day),
                    onQibla = onOpenQibla
                )
            }
        }
        items(day.times) { time ->
            PrayerRow(
                time = time,
                isNext = time == next,
                prayed = state.prayedToday.contains(time.name.name),
                alarmOn = state.enabledAlarms.contains(time.name.name),
                onTogglePrayed = { viewModel.togglePrayed(time.name, state.hijriDate) },
                onToggleAlarm = { viewModel.setAlarmEnabled(time.name, it) }
            )
        }
        item {
            QadaStrip(
                prayed = state.prayedToday,
                streak = state.streakDays,
                hijriDate = state.hijriDate,
                onToggle = { viewModel.togglePrayed(it, state.hijriDate) },
                onViewAll = onOpenTracker
            )
        }
        if (!state.exactAlarmAllowed) {
            item {
                NoorCard {
                    Text(
                        text = stringResource(R.string.prayer_exact_fallback),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }
    }
}

private fun dayProgress(day: com.exapps.nooralhuda.feature.prayer.domain.PrayerDay): Float {
    val times = day.times.map { it.at.time }.sorted()
    val now = System.currentTimeMillis()
    val start = times.firstOrNull() ?: return 0f
    val end = times.lastOrNull() ?: return 0f
    if (end <= start) return 0f
    return ((now - start).toFloat() / (end - start)).coerceIn(0f, 1f)
}

@Composable
private fun NextPrayerHero(
    next: PrayerTime,
    qibla: Double,
    progress: Float,
    onQibla: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    NoorCard {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.prayer_next),
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProgressRing(progress = progress, modifier = Modifier.size(88.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(prayerNameRes(next.name)),
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.onSurface
                    )
                    Text(
                        text = countdown(next.at.time),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFeatureSettings = "tnum",
                            fontFamily = FontFamily.Monospace
                        ),
                        color = colors.onSurface
                    )
                }
            }
            androidx.compose.material3.TextButton(onClick = onQibla) {
                Text(
                    text = stringResource(R.string.prayer_qibla, qibla.toInt()),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primary
                )
            }
        }
    }
}

private fun countdown(at: Long): String {
    val left = (at - System.currentTimeMillis()).coerceAtLeast(0)
    val h = TimeUnit.MILLISECONDS.toHours(left)
    val m = TimeUnit.MILLISECONDS.toMinutes(left) % 60
    val s = TimeUnit.MILLISECONDS.toSeconds(left) % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

@Composable
private fun ProgressRing(progress: Float, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Canvas(modifier = modifier) {
        drawArc(
            color = colors.surfaceVariant,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 10f, cap = StrokeCap.Round)
        )
        drawArc(
            color = colors.primary,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            style = Stroke(width = 10f, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun PrayerRow(
    time: PrayerTime,
    isNext: Boolean,
    prayed: Boolean,
    alarmOn: Boolean,
    onTogglePrayed: () -> Unit,
    onToggleAlarm: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    NoorCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTogglePrayed() }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(prayerNameRes(time.name)),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isNext) colors.primary else colors.onSurface
                )
                Text(
                    text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        .format(time.at),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                    color = colors.onSurfaceVariant
                )
            }
            if (time.name != PrayerName.SUNRISE) {
                IconButton(onClick = { onToggleAlarm(!alarmOn) }) {
                    Icon(
                        if (alarmOn) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                        contentDescription = null,
                        tint = if (alarmOn) colors.primary else colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QadaStrip(
    prayed: Set<String>,
    streak: Int,
    hijriDate: String,
    onToggle: (PrayerName) -> Unit,
    onViewAll: () -> Unit
) {
    NoorCard {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.prayer_qada),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.prayer_streak, streak),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                PrayerName.entries.filter { it != PrayerName.SUNRISE }.forEach { name ->
                    androidx.compose.material3.FilterChip(
                        selected = prayed.contains(name.name),
                        onClick = { onToggle(name) },
                        label = { Text(stringResource(prayerNameRes(name))) }
                    )
                }
            }
            hijriDate.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            NoorGhostButton(
                onClick = onViewAll,
                label = stringResource(R.string.prayer_view_all)
            )
        }
    }
}

fun prayerNameRes(name: PrayerName): Int = when (name) {
    PrayerName.FAJR -> R.string.prayer_fajr
    PrayerName.SUNRISE -> R.string.prayer_sunrise
    PrayerName.DHUHR -> R.string.prayer_dhuhr
    PrayerName.ASR -> R.string.prayer_asr
    PrayerName.MAGHRIB -> R.string.prayer_maghrib
    PrayerName.ISHA -> R.string.prayer_isha
}

fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    context.startActivity(
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
}

@Composable
fun HourlyDhikrRow(checked: Boolean, onChange: (Boolean) -> Unit) {
    NoorCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.prayer_hourly),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}
