package com.yourcompany.statusvault.domain.usecase

import com.yourcompany.statusvault.domain.model.SourceApp
import com.yourcompany.statusvault.domain.repository.StatusRepository
import javax.inject.Inject

class ScanStatusesUseCase @Inject constructor(
    private val statusRepository: StatusRepository,
) {
    suspend operator fun invoke(sourceApp: SourceApp) = statusRepository.scanStatuses(sourceApp)
}
