package com.exapps.nooralhuda.feature.hadith.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.navigation.HadithDetail
import com.exapps.nooralhuda.core.tts.NoorTts
import com.exapps.nooralhuda.feature.auth.domain.AuthRepository
import com.exapps.nooralhuda.feature.hadith.domain.HadithDetail
import com.exapps.nooralhuda.feature.hadith.domain.HadithRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HadithDetailUiState(
    val loading: Boolean = true,
    val detail: HadithDetail? = null,
    val errorRes: Int? = null,
    val infoRes: Int? = null,
    val signedIn: Boolean = false
)

@HiltViewModel
class HadithDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: HadithRepository,
    private val tts: NoorTts,
    auth: AuthRepository
) : ViewModel() {

    private val hadithId: String = savedStateHandle.toRoute<HadithDetail>().hadithId

    private val _uiState = MutableStateFlow(HadithDetailUiState())
    val uiState: StateFlow<HadithDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            auth.authState.collect { user ->
                _uiState.value = _uiState.value.copy(signedIn = user != null && !user.isAnonymous)
            }
        }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, errorRes = null, infoRes = null)
            repo.detail(hadithId)
                .onSuccess { detail ->
                    _uiState.value = _uiState.value.copy(loading = false, detail = detail)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        errorRes = R.string.hadith_error_load
                    )
                }
        }
    }

    fun speak() {
        _uiState.value.detail?.let { tts.speak(it.arabic) }
    }

    override fun onCleared() {
        tts.stop()
        super.onCleared()
    }

    fun copyText(context: Context) {
        _uiState.value.detail?.let { detail ->
            val text = buildShareText(detail)
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("hadith", text))
            _uiState.value = _uiState.value.copy(infoRes = R.string.hadith_copied)
        }
    }

    fun shareText(context: Context) {
        _uiState.value.detail?.let { detail ->
            val send = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, buildShareText(detail))
            context.startActivity(
                Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun report() {
        viewModelScope.launch {
            val detail = _uiState.value.detail ?: return@launch
            if (!_uiState.value.signedIn) {
                _uiState.value = _uiState.value.copy(errorRes = R.string.hadith_report_sign_in)
                return@launch
            }
            repo.reportHadith(detail.id, "inaccurate-content")
                .onSuccess {
                    _uiState.value = _uiState.value.copy(infoRes = R.string.hadith_report_sent)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorRes = R.string.hadith_error_report)
                }
        }
    }

    private fun buildShareText(detail: HadithDetail): String = buildString {
        appendLine(detail.arabic)
        detail.english?.let {
            appendLine()
            appendLine(it)
        }
        appendLine()
        append("— ${detail.source}")
    }
}
