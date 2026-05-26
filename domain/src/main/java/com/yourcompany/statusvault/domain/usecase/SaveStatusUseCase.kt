package com.yourcompany.statusvault.domain.usecase

import com.yourcompany.statusvault.domain.model.StatusItem
import com.yourcompany.statusvault.domain.repository.StatusRepository
import javax.inject.Inject

class SaveStatusUseCase @Inject constructor(
    private val statusRepository: StatusRepository,
) {
    suspend operator fun invoke(item: StatusItem): Boolean {
        return statusRepository.saveStatus(item)
    }
}
