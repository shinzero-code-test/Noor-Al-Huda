package com.exapps.nooralhuda.feature.settings.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exapps.nooralhuda.core.data.prefs.PreferencesStore
import com.exapps.nooralhuda.core.di.IoDispatcher
import com.exapps.nooralhuda.core.notifications.DhikrWorker
import com.exapps.nooralhuda.feature.auth.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val loading: Boolean = true,
    val signedIn: Boolean = false,
    val email: String? = null,
    val calcMethod: String = "ummAlQura",
    val fontFamily: String = "naskh",
    val fontScale: Float = 1f,
    val notifications: Boolean = true,
    val hourlyDhikr: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: PreferencesStore,
    private val auth: AuthRepository,
    private val db: com.exapps.nooralhuda.core.data.db.NoorDatabase,
    @IoDispatcher private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = auth.authState.first()
            _uiState.value = SettingsUiState(
                loading = false,
                signedIn = user != null,
                email = user?.email,
                calcMethod = prefs.calcMethod() ?: "ummAlQura",
                fontFamily = prefs.getString(PreferencesStore.Keys.QURAN_FONT_FAMILY) ?: "naskh",
                fontScale = prefs.getDouble(PreferencesStore.Keys.QURAN_FONT_SCALE)?.toFloat() ?: 1f,
                notifications = prefs.getBoolean(PreferencesStore.Keys.NOTIFICATIONS, true),
                hourlyDhikr = prefs.getBoolean(PreferencesStore.Keys.HOURLY_DHIKR, true)
            )
        }
    }

    fun setCalcMethod(method: String) {
        _uiState.value = _uiState.value.copy(calcMethod = method)
        viewModelScope.launch { prefs.setCalcMethod(method) }
    }

    fun setFontFamily(family: String) {
        _uiState.value = _uiState.value.copy(fontFamily = family)
        viewModelScope.launch { prefs.setString(PreferencesStore.Keys.QURAN_FONT_FAMILY, family) }
    }

    fun setFontScale(scale: Float) {
        _uiState.value = _uiState.value.copy(fontScale = scale)
        viewModelScope.launch { prefs.setDouble(PreferencesStore.Keys.QURAN_FONT_SCALE, scale.toDouble()) }
    }

    fun setNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notifications = enabled)
        viewModelScope.launch { prefs.setBoolean(PreferencesStore.Keys.NOTIFICATIONS, enabled) }
    }

    fun setHourlyDhikr(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(hourlyDhikr = enabled)
        viewModelScope.launch {
            prefs.setBoolean(PreferencesStore.Keys.HOURLY_DHIKR, enabled)
            DhikrWorker.setEnabled(context, enabled)
        }
    }

    fun wipe() {
        viewModelScope.launch {
            // Local wipe only: Room, preferences, downloaded audio. The Firebase
            // account itself is deleted from the console, never from here.
            kotlinx.coroutines.withContext(ioDispatcher) {
                db.clearAllTables()
                prefs.clearAll()
                java.io.File(context.filesDir, "audio").deleteRecursively()
            }
            _uiState.value = SettingsUiState(loading = false)
        }
    }
}
