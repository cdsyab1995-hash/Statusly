package com.yourcompany.statusvault.feature.statuslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.statusvault.core.common.SelectedSourceHolder
import com.yourcompany.statusvault.domain.model.DirectoryGrant
import com.yourcompany.statusvault.domain.model.SourceApp
import com.yourcompany.statusvault.domain.model.StatusItem
import com.yourcompany.statusvault.domain.model.StatusScanDebug
import com.yourcompany.statusvault.domain.repository.DirectoryRepository
import com.yourcompany.statusvault.domain.usecase.GetLastStatusScanDebugUseCase
import com.yourcompany.statusvault.domain.usecase.GetSavedStatusIdsUseCase
import com.yourcompany.statusvault.domain.usecase.SaveStatusUseCase
import com.yourcompany.statusvault.domain.usecase.ScanStatusesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatusListUiState(
    val isLoading: Boolean = false,
    val hasGrant: Boolean = false,
    val needsDirectoryReconnect: Boolean = false,
    val items: List<StatusItem> = emptyList(),
    val grantPathHint: String? = null,
    val scanDebug: StatusScanDebug? = null,
    val message: String? = null,
)

@HiltViewModel
class StatusListViewModel @Inject constructor(
    private val directoryRepository: DirectoryRepository,
    private val scanStatusesUseCase: ScanStatusesUseCase,
    private val saveStatusUseCase: SaveStatusUseCase,
    private val getSavedStatusIdsUseCase: GetSavedStatusIdsUseCase,
    private val getLastStatusScanDebugUseCase: GetLastStatusScanDebugUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatusListUiState())
    val uiState: StateFlow<StatusListUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun loadStatuses(
        sourceApp: SourceApp = SelectedSourceHolder.sourceApp,
        fromReturn: Boolean = false,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)

            runCatching {
                val grant = directoryRepository.getGrant(sourceApp)
                if (grant == null || !grant.isValid) {
                    _uiState.value = StatusListUiState(
                        isLoading = false,
                        hasGrant = false,
                        needsDirectoryReconnect = false,
                        items = emptyList(),
                        grantPathHint = null,
                        scanDebug = null,
                        message = "Grant access to the WhatsApp status folder before scanning.",
                    )
                    return@launch
                }

                val grantPathHint = grant.treeUri
                val grantMatchesStatusFolder = directoryRepository.validateGrant(sourceApp)
                if (!grantMatchesStatusFolder) {
                    _uiState.value = StatusListUiState(
                        isLoading = false,
                        hasGrant = false,
                        needsDirectoryReconnect = true,
                        items = emptyList(),
                        grantPathHint = grantPathHint,
                        scanDebug = null,
                        message = "Wrong folder selected or the device denied access. Please authorize WhatsApp access again.",
                    )
                    return@launch
                }

                val items = scanStatusesUseCase(sourceApp)
                val savedIds = getSavedStatusIdsUseCase()
                val scanDebug = getLastStatusScanDebugUseCase()
                _uiState.value = StatusListUiState(
                    isLoading = false,
                    hasGrant = true,
                    needsDirectoryReconnect = false,
                    items = items.map { it.copy(isSaved = savedIds.contains(it.id)) },
                    grantPathHint = grantPathHint,
                    scanDebug = scanDebug,
                    message = when {
                        items.isEmpty() && fromReturn -> {
                            "No viewed statuses detected yet. The folder is connected, but WhatsApp may not have written any visible status files yet."
                        }

                        items.isEmpty() -> {
                            "Folder connected. Open at least one WhatsApp status, then come back to scan. For videos, wait until playback starts and the file finishes loading."
                        }

                        else -> null
                    },
                )
            }.onFailure {
                _uiState.value = StatusListUiState(
                    isLoading = false,
                    hasGrant = false,
                    needsDirectoryReconnect = true,
                    items = emptyList(),
                    grantPathHint = null,
                    scanDebug = null,
                    message = "Authorization failed on this device. Please authorize WhatsApp access again.",
                )
            }
        }
    }

    fun save(item: StatusItem) {
        viewModelScope.launch {
            runCatching {
                saveStatusUseCase(item)
            }.onSuccess { success ->
                _uiState.value = _uiState.value.copy(
                    items = _uiState.value.items.map {
                        if (it.id == item.id && success) it.copy(isSaved = true) else it
                    },
                )
                _events.tryEmit(if (success) "Saved to gallery" else "Save failed")
            }.onFailure {
                _events.tryEmit("Save failed")
            }
        }
    }

    fun saveGrant(treeUri: String) {
        viewModelScope.launch {
            runCatching {
                directoryRepository.saveGrant(
                    DirectoryGrant(
                        sourceApp = SelectedSourceHolder.sourceApp,
                        treeUri = treeUri,
                        isValid = true,
                        grantedAt = System.currentTimeMillis(),
                    ),
                )
            }.onSuccess {
                loadStatuses()
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasGrant = false,
                    needsDirectoryReconnect = true,
                    message = "Unable to save the granted folder on this device. Please try again.",
                )
            }
        }
    }
}
