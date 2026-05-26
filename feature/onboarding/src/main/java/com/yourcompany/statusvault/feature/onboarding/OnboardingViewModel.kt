package com.yourcompany.statusvault.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.statusvault.domain.model.DirectoryGrant
import com.yourcompany.statusvault.domain.model.SourceApp
import com.yourcompany.statusvault.domain.repository.DirectoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val hasGrant: Boolean = false,
    val selectedSourceApp: SourceApp = SourceApp.WHATSAPP,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val directoryRepository: DirectoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        refreshGrantState()
    }

    fun onSourceAppSelected(sourceApp: SourceApp) {
        _uiState.value = _uiState.value.copy(selectedSourceApp = sourceApp)
        refreshGrantState()
    }

    fun saveGrant(treeUri: String) {
        viewModelScope.launch {
            directoryRepository.saveGrant(
                DirectoryGrant(
                    sourceApp = _uiState.value.selectedSourceApp,
                    treeUri = treeUri,
                    isValid = true,
                    grantedAt = System.currentTimeMillis(),
                ),
            )
            _uiState.value = _uiState.value.copy(hasGrant = true)
        }
    }

    private fun refreshGrantState() {
        viewModelScope.launch {
            val sourceApp = _uiState.value.selectedSourceApp
            val hasGrant = directoryRepository.getGrant(sourceApp)?.isValid == true
            _uiState.value = _uiState.value.copy(hasGrant = hasGrant)
        }
    }
}
