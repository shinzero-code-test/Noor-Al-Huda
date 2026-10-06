package com.exapps.nooralhuda.feature.khatm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.feature.khatm.domain.KhatmGroup
import com.exapps.nooralhuda.feature.khatm.domain.KhatmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KhatmListUiState(
    val mine: List<KhatmGroup> = emptyList(),
    val open: List<KhatmGroup> = emptyList(),
    val groupName: String = "",
    val openJoin: Boolean = false,
    val inviteCode: String = "",
    val busy: Boolean = false,
    val errorRes: Int? = null,
    val infoRes: Int? = null
)

@HiltViewModel
class KhatmListViewModel @Inject constructor(
    private val repo: KhatmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KhatmListUiState())
    val uiState: StateFlow<KhatmListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.myGroups().collect { mine ->
                _uiState.value = _uiState.value.copy(mine = mine)
            }
        }
        viewModelScope.launch {
            repo.openGroups().collect { open ->
                _uiState.value = _uiState.value.copy(open = open)
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name, errorRes = null)
    }

    fun onOpenJoinChange(open: Boolean) {
        _uiState.value = _uiState.value.copy(openJoin = open)
    }

    fun onCodeChange(code: String) {
        _uiState.value = _uiState.value.copy(inviteCode = code, errorRes = null)
    }

    fun createGroup() {
        val name = _uiState.value.groupName.trim()
        if (name.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorRes = R.string.khatm_error_name)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(busy = true, errorRes = null, infoRes = null)
            repo.createGroup(name, _uiState.value.openJoin)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        busy = false, groupName = "",
                        infoRes = R.string.khatm_created
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        busy = false, errorRes = R.string.khatm_error_create
                    )
                }
        }
    }

    fun joinWithCode(onJoined: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(busy = true, errorRes = null, infoRes = null)
            repo.joinWithCode(_uiState.value.inviteCode)
                .onSuccess { group ->
                    _uiState.value = _uiState.value.copy(busy = false, inviteCode = "")
                    onJoined(group.id)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        busy = false, errorRes = R.string.khatm_error_join
                    )
                }
        }
    }

    fun joinOpen(group: KhatmGroup) {
        viewModelScope.launch {
            repo.joinOpen(group.id)
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorRes = R.string.khatm_error_join)
                }
        }
    }
}
