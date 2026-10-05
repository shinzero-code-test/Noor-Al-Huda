package com.exapps.nooralhuda.feature.settings.data

import com.exapps.nooralhuda.core.data.prefs.PreferencesStore
import com.exapps.nooralhuda.core.privacy.PrivacyManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Settings live in DataStore locally; Firestore `users/{uid}` mirrors them.
 * Last-write-wins by updatedAt. No sync under privacy mode.
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val prefs: PreferencesStore,
    private val firestore: FirebaseFirestore,
    private val privacy: PrivacyManager
) {
    suspend fun push(uid: String, settings: Map<String, Any>) {
        if (!privacy.canSync()) return
        firestore.collection("users").document(uid)
            .set(
                mapOf(
                    "settings" to settings,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
            .await()
        prefs.setLastSync("settings", System.currentTimeMillis())
    }

    suspend fun pull(uid: String): Map<String, Any>? {
        if (!privacy.canSync()) return null
        val doc = firestore.collection("users").document(uid).get().await()
        @Suppress("UNCHECKED_CAST")
        return doc.get("settings") as? Map<String, Any>
    }

    suspend fun lastSync(): Long? = prefs.lastSync("settings")
}
