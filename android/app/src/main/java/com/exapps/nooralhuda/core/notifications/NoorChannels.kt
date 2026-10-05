package com.exapps.nooralhuda.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/** Created once in NoorApp. Channel IDs are stable — never rename them. */
object NoorChannels {
    const val AZAN = "azan"
    const val DHIKR = "dhikr"
    const val GENERAL = "general"
}

fun createNoorChannels(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannels(
        listOf(
            NotificationChannel(
                NoorChannels.AZAN,
                "Adhan",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Prayer time calls" },
            NotificationChannel(
                NoorChannels.DHIKR,
                "Dhikr",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Remembrance reminders" },
            NotificationChannel(
                NoorChannels.GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    )
}
