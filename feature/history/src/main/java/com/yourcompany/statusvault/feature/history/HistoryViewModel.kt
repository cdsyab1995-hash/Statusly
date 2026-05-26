package com.yourcompany.statusvault.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.statusvault.domain.model.SavedItem
import com.yourcompany.statusvault.domain.usecase.ObserveSavedHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HistoryUiState(
    val items: List<SavedItem> = emptyList(),
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    observeSavedHistoryUseCase: ObserveSavedHistoryUseCase,
) : ViewModel() {
    val uiState: StateFlow<HistoryUiState> = observeSavedHistoryUseCase()
        .map { HistoryUiState(items = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(),
        )
}
