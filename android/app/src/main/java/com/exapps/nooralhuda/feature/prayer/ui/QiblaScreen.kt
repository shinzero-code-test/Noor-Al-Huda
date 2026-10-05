package com.exapps.nooralhuda.feature.prayer.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exapps.nooralhuda.R
import kotlin.math.abs

/** Simple compass: rotation-vector azimuth vs the computed Qibla bearing. Camera overlay later. */
@Composable
fun QiblaScreen(viewModel: PrayerViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var azimuth by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val manager = context.getSystemService(SensorManager::class.java) ?: return@DisposableEffect onDispose {}
        val rotation = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val fallback = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        val sensor = rotation ?: fallback
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                azimuth = if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val matrix = FloatArray(9)
                    val orientation = FloatArray(3)
                    SensorManager.getRotationMatrixFromVector(matrix, event.values)
                    SensorManager.getOrientation(matrix, orientation)
                    Math.toDegrees(orientation[0].toDouble()).toFloat()
                } else {
                    event.values[0]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        if (sensor != null) {
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose { manager.unregisterListener(listener) }
    }

    val qibla = state.day?.qiblaDegrees?.toFloat() ?: 0f
    val delta = ((qibla - azimuth + 540f) % 360f) - 180f
    val aligned = abs(delta) <= 2f

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.qibla_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Canvas(modifier = Modifier.size(240.dp)) {
            drawCircle(color = androidx.compose.ui.graphics.Color(0xFF1C1A16))
            rotate(degrees = -delta) {
                drawLine(
                    color = androidx.compose.ui.graphics.Color(0xFFE5C158),
                    start = center,
                    end = center + Offset(0f, -size.minDimension / 2 + 24f),
                    strokeWidth = 12f
                )
            }
            drawCircle(color = androidx.compose.ui.graphics.Color(0xFFD4AF37))
        }
        Text(
            text = stringResource(R.string.qibla_degrees, qibla.toInt()),
            style = MaterialTheme.typography.titleLarge,
            color = if (aligned) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
