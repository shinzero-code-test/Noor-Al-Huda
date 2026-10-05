package com.exapps.nooralhuda.core.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.noorDataStore: DataStore<Preferences> by preferencesDataStore(name = "noor_prefs")

/** Small preferences only. Lists and content live in Room. */
@Singleton
class PreferencesStore @Inject constructor(@ApplicationContext private val context: Context) :
    PendingEmailStore {

    object Keys {
        val PRIVACY_MODE = stringPreferencesKey("privacy_mode")
        val LOCALE_TAG = stringPreferencesKey("locale_tag")
        val LAST_READ_SURAH = stringPreferencesKey("last_read_surah")
        /** Passwordless email-link address. Not a secret — plain DataStore by design. */
        val PENDING_EMAIL_LINK = stringPreferencesKey("pending_email_link")
    }

    fun lastSyncKey(bucket: String) = "sync:last:$bucket"

    val privacyMode: Flow<String?> = context.noorDataStore.data.map { it[Keys.PRIVACY_MODE] }
    val localeTag: Flow<String?> = context.noorDataStore.data.map { it[Keys.LOCALE_TAG] }

    suspend fun setPrivacyMode(mode: String) {
        context.noorDataStore.edit { it[Keys.PRIVACY_MODE] = mode }
    }

    suspend fun setLocaleTag(tag: String) {
        context.noorDataStore.edit { it[Keys.LOCALE_TAG] = tag }
    }

    override suspend fun setLastReadSurah(surahId: Int) {
        context.noorDataStore.edit { it[Keys.LAST_READ_SURAH] = surahId.toString() }
    }

    override suspend fun lastReadSurahId(): Int? {
        return context.noorDataStore.data.map { it[Keys.LAST_READ_SURAH]?.toIntOrNull() }.first()
    }

    suspend fun setLastSync(bucket: String, at: Long) {
        context.noorDataStore.edit { it[longPreferencesKey(lastSyncKey(bucket))] = at }
    }

    suspend fun lastSync(bucket: String): Long? {
        return context.noorDataStore.data.map { it[longPreferencesKey(lastSyncKey(bucket))] }.first()
    }

    override suspend fun setPendingEmail(email: String?) {
        context.noorDataStore.edit {
            if (email == null) it.remove(Keys.PENDING_EMAIL_LINK)
            else it[Keys.PENDING_EMAIL_LINK] = email
        }
    }

    override suspend fun pendingEmail(): String? {
        return context.noorDataStore.data.map { it[Keys.PENDING_EMAIL_LINK] }.first()
    }
}
