package com.yourcompany.statusvault.domain.usecase

import com.yourcompany.statusvault.domain.repository.StatusRepository
import javax.inject.Inject

class ObserveSavedHistoryUseCase @Inject constructor(
    private val statusRepository: StatusRepository,
) {
    operator fun invoke() = statusRepository.observeSavedHistory()
}
