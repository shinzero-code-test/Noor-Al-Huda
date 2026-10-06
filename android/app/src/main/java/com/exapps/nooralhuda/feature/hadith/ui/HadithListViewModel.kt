package com.exapps.nooralhuda.feature.hadith.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.exapps.nooralhuda.feature.hadith.domain.HadithCollection
import com.exapps.nooralhuda.feature.hadith.domain.HadithItem
import com.exapps.nooralhuda.feature.hadith.domain.HadithRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HadithListUiState(
    val collections: List<HadithCollection> = emptyList(),
    val selectedId: String = "1",
    /** Client-side filter over loaded items (same as the legacy client). */
    val query: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HadithListViewModel @Inject constructor(
    private val repo: HadithRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HadithListUiState())
    val uiState: StateFlow<HadithListUiState> = _uiState.asStateFlow()

    val items: Flow<PagingData<HadithItem>> =
        _uiState.flatMapLatest { repo.itemsPager(it.selectedId) }.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            repo.collections.collect { collections ->
                val current = _uiState.value
                val selected = if (collections.any { it.id == current.selectedId }) {
                    current.selectedId
                } else {
                    collections.firstOrNull()?.id ?: current.selectedId
                }
                _uiState.value = current.copy(collections = collections, selectedId = selected)
            }
        }
        viewModelScope.launch { repo.refreshCollections() }
    }

    fun selectCollection(id: String) {
        _uiState.value = _uiState.value.copy(selectedId = id)
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun retry() {
        viewModelScope.launch { repo.refreshCollections() }
    }
}
