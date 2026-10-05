package com.exapps.nooralhuda.core.notifications

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.core.app.NotificationCompat
import com.exapps.nooralhuda.R

/** Data-message display + FCM token registration under the owner's subtree. */
class NoorMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.data["title"] ?: getString(R.string.app_name)
        val body = message.data["body"] ?: return
        val notification = NotificationCompat.Builder(this, NoorChannels.GENERAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        getSystemService(android.app.NotificationManager::class.java)
            .notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }

    override fun onNewToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .collection("devices").document(token)
            .set(mapOf("token" to token, "updatedAt" to System.currentTimeMillis()))
    }
}
