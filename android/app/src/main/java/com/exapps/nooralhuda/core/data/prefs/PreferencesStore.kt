package com.exapps.nooralhuda.core.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
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
        val PRAYER_LAT = doublePreferencesKey("prayer_lat")
        val PRAYER_LNG = doublePreferencesKey("prayer_lng")
        val PRAYER_LABEL = stringPreferencesKey("prayer_label")
        val CALC_METHOD = stringPreferencesKey("calc_method")
        val ENABLED_PRAYERS = stringPreferencesKey("enabled_prayers_csv")
        val RECITER_ID = stringPreferencesKey("reciter_id")
        val QURAN_FONT_SCALE = doublePreferencesKey("quran_font_scale")
        val QURAN_FONT_FAMILY = stringPreferencesKey("quran_font_family")
        val ADHAN_SOUND = stringPreferencesKey("adhan_sound")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val HOURLY_DHIKR = booleanPreferencesKey("hourly_dhikr")
        val MORNING_EVENING = booleanPreferencesKey("morning_evening")
        val LAST_RADIO_ID = stringPreferencesKey("last_radio_id")
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

    suspend fun clearAll() {
        context.noorDataStore.edit { it.clear() }
    }

    override suspend fun pendingEmail(): String? {
        return context.noorDataStore.data.map { it[Keys.PENDING_EMAIL_LINK] }.first()
    }

    suspend fun setLastRadioId(id: String) {
        context.noorDataStore.edit { it[Keys.LAST_RADIO_ID] = id }
    }

    suspend fun lastRadioId(): String? {
        return context.noorDataStore.data.map { it[Keys.LAST_RADIO_ID] }.first()
    }

    suspend fun setPrayerLocation(lat: Double, lng: Double, label: String) {
        context.noorDataStore.edit {
            it[Keys.PRAYER_LAT] = lat
            it[Keys.PRAYER_LNG] = lng
            it[Keys.PRAYER_LABEL] = label
        }
    }

    suspend fun prayerLocation(): com.exapps.nooralhuda.feature.prayer.data.DeviceLocation? {
        val prefs = context.noorDataStore.data.first()
        val lat = prefs[Keys.PRAYER_LAT] ?: return null
        val lng = prefs[Keys.PRAYER_LNG] ?: return null
        return com.exapps.nooralhuda.feature.prayer.data.DeviceLocation(
            lat, lng, prefs[Keys.PRAYER_LABEL].orEmpty()
        )
    }

    suspend fun setCalcMethod(method: String) {
        context.noorDataStore.edit { it[Keys.CALC_METHOD] = method }
    }

    suspend fun calcMethod(): String? {
        return context.noorDataStore.data.map { it[Keys.CALC_METHOD] }.first()
    }

    suspend fun setEnabledPrayers(names: Set<String>) {
        context.noorDataStore.edit { it[Keys.ENABLED_PRAYERS] = names.joinToString(",") }
    }

    suspend fun enabledPrayers(): Set<String> {
        val raw = context.noorDataStore.data.map { it[Keys.ENABLED_PRAYERS] }.first()
        return raw?.split(",")?.filter { it.isNotBlank() }?.toSet().orEmpty()
    }

    suspend fun setString(key: androidx.datastore.preferences.core.Preferences.Key<String>, value: String) {
        context.noorDataStore.edit { it[key] = value }
    }

    suspend fun getString(key: androidx.datastore.preferences.core.Preferences.Key<String>): String? {
        return context.noorDataStore.data.map { it[key] }.first()
    }

    suspend fun setBoolean(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, value: Boolean) {
        context.noorDataStore.edit { it[key] = value }
    }

    suspend fun getBoolean(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, default: Boolean = true): Boolean {
        return context.noorDataStore.data.map { it[key] }.first() ?: default
    }

    suspend fun setDouble(key: androidx.datastore.preferences.core.Preferences.Key<Double>, value: Double) {
        context.noorDataStore.edit { it[key] = value }
    }

    suspend fun getDouble(key: androidx.datastore.preferences.core.Preferences.Key<Double>): Double? {
        return context.noorDataStore.data.map { it[key] }.first()
    }
}
