package com.exapps.nooralhuda.core.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.exapps.nooralhuda.core.data.prefs.PreferencesStore
import com.exapps.nooralhuda.core.privacy.PrivacyManager
import com.exapps.nooralhuda.feature.auth.domain.AuthRepository
import com.exapps.nooralhuda.feature.bookmarks.data.BookmarkRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/** Manual Hilt entry point (no hilt-work dependency by design). */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SyncWorkerEntryPoint {
    fun auth(): AuthRepository
    fun bookmarks(): BookmarkRepository
    fun privacy(): PrivacyManager
    fun prefs(): PreferencesStore
}

class SyncWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val entry = EntryPointAccessors.fromApplication(
            applicationContext,
            SyncWorkerEntryPoint::class.java
        )
        if (!entry.privacy().canSync()) return Result.success()
        val uid = entry.auth().authState.first()?.uid ?: return Result.success()
        return try {
            entry.bookmarks().push(uid)
            entry.bookmarks().pull(uid)
            entry.prefs().setLastSync("bookmarks", System.currentTimeMillis())
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}

object SyncScheduler {
    private const val PERIODIC = "sync-periodic"
    private const val NOW = "sync-now"

    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC, ExistingPeriodicWorkPolicy.KEEP, request
        )
    }

    fun syncNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            NOW, ExistingWorkPolicy.REPLACE, request
        )
    }
}
