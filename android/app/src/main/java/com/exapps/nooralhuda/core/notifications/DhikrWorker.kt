package com.exapps.nooralhuda.core.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.exapps.nooralhuda.R
import java.util.concurrent.TimeUnit

/** Hourly remembrance nudge. Quiet hours (23–4) are skipped locally. */
class DhikrWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        if (hour >= 23 || hour <= 4) return Result.success()
        val notification = NotificationCompat.Builder(applicationContext, NoorChannels.DHIKR)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(applicationContext.getString(R.string.dhikr_title))
            .setContentText(applicationContext.getString(R.string.dhikr_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        applicationContext.getSystemService(android.app.NotificationManager::class.java)
            .notify(2001, notification)
        return Result.success()
    }

    companion object {
        private const val NAME = "dhikr-hourly"

        fun setEnabled(context: Context, enabled: Boolean) {
            val work = WorkManager.getInstance(context)
            if (!enabled) {
                work.cancelUniqueWork(NAME)
                return
            }
            work.enqueueUniquePeriodicWork(
                NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<DhikrWorker>(1, TimeUnit.HOURS).build()
            )
        }
    }
}
