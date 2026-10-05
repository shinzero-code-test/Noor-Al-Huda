package com.exapps.nooralhuda.core.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Manual Firebase init (no google-services plugin/json).
 *
 * These values are PUBLIC client identifiers — they ship inside every Firebase
 * Android app and are not secrets. Security is enforced by Firestore rules,
 * App Check, and the backend's token verification, never by hiding these.
 * Verified from the new project's SDK config (2026-10-04).
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseApp(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): FirebaseApp {
        return FirebaseApp.getApps(context).firstOrNull()
            ?: FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApiKey("AIzaSyClhn-sBQl39VFqu5IxcJjsEueRxwjsBns")
                    .setApplicationId("1:170730557392:android:188a7df04b90eb443a33d5")
                    .setProjectId("nooralhuda-2026")
                    .setStorageBucket("nooralhuda-2026.firebasestorage.app")
                    .setGcmSenderId("170730557392")
                    .build()
            )!!
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(app: FirebaseApp): com.google.firebase.auth.FirebaseAuth =
        com.google.firebase.auth.FirebaseAuth.getInstance(app)

    @Provides
    @Singleton
    fun provideFirestore(app: FirebaseApp): com.google.firebase.firestore.FirebaseFirestore =
        com.google.firebase.firestore.FirebaseFirestore.getInstance(app)
}
