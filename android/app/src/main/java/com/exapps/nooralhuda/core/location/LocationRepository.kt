package com.exapps.nooralhuda.core.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class LatLng(val lat: Double, val lng: Double)

/** Fused location. Callers must hold location permission first. */
@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    @SuppressLint("MissingPermission")
    suspend fun lastKnown(): LatLng? {
        return try {
            client.lastLocation.await()?.let { LatLng(it.latitude, it.longitude) }
        } catch (_: Exception) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun fresh(): LatLng? {
        return try {
            val token = CancellationTokenSource()
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, token.token)
                .await()
                ?.let { LatLng(it.latitude, it.longitude) }
        } catch (_: Exception) {
            null
        }
    }
}
