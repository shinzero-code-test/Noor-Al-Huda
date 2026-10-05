package com.exapps.nooralhuda.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.feature.prayer.domain.PrayerName
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/** Fires at prayer time from AlarmManager. Shows the adhan notification. */
class AdhanReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(EXTRA_PRAYER) ?: return
        val label = intent.getStringExtra(EXTRA_LABEL) ?: name
        val notification = NotificationCompat.Builder(context, NoorChannels.AZAN)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(label)
            .setContentText(context.getString(R.string.notif_prayer_time))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val manager = context.getSystemService(android.app.NotificationManager::class.java)
        manager.notify(NOTIF_ID + name.hashCode(), notification)
    }

    companion object {
        const val EXTRA_PRAYER = "prayer"
        const val EXTRA_LABEL = "label"
        private const val NOTIF_ID = 1000

        fun pending(context: Context, prayer: PrayerName, label: String): PendingIntent {
            val intent = Intent(context, AdhanReceiver::class.java).apply {
                putExtra(EXTRA_PRAYER, prayer.name)
                putExtra(EXTRA_LABEL, label)
            }
            return PendingIntent.getBroadcast(
                context,
                prayer.ordinal,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}

/** Exact alarm when allowed, inexact best-effort otherwise. Never crashes when denied. */
object PrayerAlarmScheduler {
    fun schedule(context: Context, prayer: PrayerName, label: String, triggerAt: Long) {
        if (triggerAt <= System.currentTimeMillis()) return
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val operation = AdhanReceiver.pending(context, prayer, label)
        // Pre-S exact alarms need no permission; S+ requires SCHEDULE_EXACT_ALARM.
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S ||
            manager.canScheduleExactAlarms()
        ) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
        } else {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
        }
    }

    fun cancel(context: Context, prayer: PrayerName) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        manager.cancel(AdhanReceiver.pending(context, prayer, ""))
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RescheduleEntryPoint {
    fun scheduler(): com.exapps.nooralhuda.feature.prayer.data.PrayerScheduler
}

/** Recomputes today + tomorrow and schedules every enabled prayer. */
class RescheduleWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val entry = EntryPointAccessors.fromApplication(
            applicationContext,
            RescheduleEntryPoint::class.java
        )
        return try {
            entry.scheduler().rescheduleAll()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun enqueue(context: Context) {
            WorkManager.getInstance(context)
                .enqueue(OneTimeWorkRequestBuilder<RescheduleWorker>().build())
        }
    }
}

/** Re-schedules prayer alarms after reboot. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        RescheduleWorker.enqueue(context)
    }
}
