package com.exapps.nooralhuda.feature.khatm.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.navigation.KhatmDetail
import com.exapps.nooralhuda.feature.khatm.domain.KhatmMember
import com.exapps.nooralhuda.feature.khatm.domain.KhatmRepository
import com.exapps.nooralhuda.feature.khatm.domain.khatmProgress
import com.exapps.nooralhuda.feature.khatm.domain.nextUnclaimedPage
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KhatmDetailUiState(
    val groupName: String = "",
    val isCreator: Boolean = false,
    val inviteCode: String? = null,
    val members: List<KhatmMember> = emptyList(),
    val donePages: Int = 0,
    val percent: Int = 0,
    val nextPage: Int? = null,
    val busy: Boolean = false,
    val errorRes: Int? = null
)

@HiltViewModel
class KhatmDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: KhatmRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val args: KhatmDetail = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(
        KhatmDetailUiState(
            groupName = args.name,
            isCreator = args.creatorId == auth.currentUser?.uid
        )
    )
    val uiState: StateFlow<KhatmDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.members(args.groupId).collect { members ->
                val progress = khatmProgress(members)
                _uiState.value = _uiState.value.copy(
                    members = members,
                    donePages = progress.donePages,
                    percent = progress.percent,
                    nextPage = nextUnclaimedPage(members)
                )
            }
        }
        viewModelScope.launch {
            val code = repo.inviteCodeFor(args.groupId)
            _uiState.value = _uiState.value.copy(inviteCode = code)
        }
    }

    fun markNextDone() {
        val page = _uiState.value.nextPage ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(busy = true, errorRes = null)
            repo.markPageDone(args.groupId, page)
                .onSuccess { _uiState.value = _uiState.value.copy(busy = false) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        busy = false, errorRes = R.string.khatm_error_progress
                    )
                }
        }
    }

    fun leave(onGone: () -> Unit) {
        viewModelScope.launch {
            repo.leave(args.groupId)
                .onSuccess { onGone() }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorRes = R.string.khatm_error_leave)
                }
        }
    }

    fun removeMember(member: KhatmMember) {
        viewModelScope.launch {
            repo.removeMember(args.groupId, member.uid)
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorRes = R.string.khatm_error_remove)
                }
        }
    }

    fun shareCode(code: String, context: Context) {
        val send = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, code)
        context.startActivity(
            Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
