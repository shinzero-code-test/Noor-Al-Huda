package com.exapps.nooralhuda

import android.app.Application
import com.exapps.nooralhuda.core.notifications.RescheduleWorker
import com.exapps.nooralhuda.core.notifications.createNoorChannels
import com.exapps.nooralhuda.core.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NoorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNoorChannels(this)
        SyncScheduler.schedulePeriodic(this)
        RescheduleWorker.enqueue(this)
    }
}
