package com.exapps.nooralhuda.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.data.prefs.PendingEmailStore
import com.exapps.nooralhuda.feature.auth.data.AuthException
import com.exapps.nooralhuda.feature.auth.domain.AuthError
import com.exapps.nooralhuda.feature.auth.domain.AuthRepository
import com.exapps.nooralhuda.feature.auth.domain.NoorUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val busy: Boolean = false,
    val user: NoorUser? = null,
    val errorRes: Int? = null,
    val infoRes: Int? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val prefs: PendingEmailStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** One-shot navigation/UX events. */
    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            auth.authState.collect { user ->
                _uiState.value = _uiState.value.copy(user = user, busy = false)
                if (user != null) _events.emit(AuthEvent.SignedIn)
            }
        }
    }

    fun signIn(email: String, password: String) = runBusy {
        auth.signIn(email, password).mapError().onSuccess { clearError() }
    }

    fun register(email: String, password: String) = runBusy {
        auth.register(email, password).mapError().onSuccess { clearError() }
    }

    fun continueAsGuest() = runBusy {
        auth.signInAnonymously().mapError().onSuccess { clearError() }
    }

    fun sendReset(email: String) = runBusy {
        auth.sendPasswordReset(email).mapError()
            .onSuccess { setInfo(R.string.auth_reset_sent) }
    }

    fun sendLink(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(busy = true, errorRes = null, infoRes = null)
            auth.sendEmailLink(email)
                .onSuccess {
                    prefs.setPendingEmail(email.trim())
                    _uiState.value = _uiState.value.copy(busy = false, infoRes = R.string.auth_link_sent)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(busy = false, errorRes = errorResOf(error))
                }
        }
    }

    /** Called by MainActivity for VIEW intents carrying a Firebase email link. */
    fun consumeEmailLink(link: String) {
        viewModelScope.launch {
            if (!auth.isEmailLink(link)) return@launch
            val email = prefs.pendingEmail()
            if (email.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(errorRes = R.string.auth_link_needs_email)
                _events.emit(AuthEvent.LinkNeedsEmail)
                return@launch
            }
            _uiState.value = _uiState.value.copy(busy = true, errorRes = null)
            auth.completeEmailLink(link, email)
                .onSuccess { prefs.setPendingEmail(null) }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(busy = false, errorRes = errorResOf(error))
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            auth.signOut()
            _events.emit(AuthEvent.SignedOut)
        }
    }

    private fun runBusy(block: suspend () -> Result<*>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(busy = true, errorRes = null, infoRes = null)
            block()
            _uiState.value = _uiState.value.copy(busy = false)
        }
    }

    private suspend fun <T> Result<T>.mapError(): Result<T> = onFailure { error ->
        _uiState.value = _uiState.value.copy(errorRes = errorResOf(error))
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(errorRes = null)
    }

    private fun setInfo(res: Int) {
        _uiState.value = _uiState.value.copy(infoRes = res)
    }

    companion object {
        fun errorResOf(error: Throwable): Int = when ((error as? AuthException)?.error) {
            AuthError.InvalidCredentials -> R.string.auth_error_invalid
            AuthError.EmailInUse -> R.string.auth_error_in_use
            AuthError.WeakPassword -> R.string.auth_error_weak
            AuthError.UserNotFound -> R.string.auth_error_not_found
            AuthError.Network -> R.string.auth_error_network
            AuthError.TooManyRequests -> R.string.auth_error_rate_limited
            AuthError.LinkExpired -> R.string.auth_error_link_expired
            AuthError.RequiresRecentLogin -> R.string.auth_error_recent_login
            is AuthError.Unknown, null -> R.string.auth_error_unknown
        }
    }
}

sealed interface AuthEvent {
    data object SignedIn : AuthEvent
    data object SignedOut : AuthEvent
    data object LinkNeedsEmail : AuthEvent
}
