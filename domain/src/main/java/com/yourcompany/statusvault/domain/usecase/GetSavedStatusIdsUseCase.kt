package com.yourcompany.statusvault.domain.usecase

import com.yourcompany.statusvault.domain.repository.StatusRepository
import javax.inject.Inject

class GetSavedStatusIdsUseCase @Inject constructor(
    private val statusRepository: StatusRepository,
) {
    suspend operator fun invoke(): Set<String> = statusRepository.getSavedIds()
}
