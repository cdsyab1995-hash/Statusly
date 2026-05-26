package com.yourcompany.statusvault.domain.repository

import com.yourcompany.statusvault.domain.model.SavedItem
import com.yourcompany.statusvault.domain.model.SourceApp
import com.yourcompany.statusvault.domain.model.StatusItem
import com.yourcompany.statusvault.domain.model.StatusScanDebug
import kotlinx.coroutines.flow.Flow

interface StatusRepository {
    suspend fun scanStatuses(sourceApp: SourceApp): List<StatusItem>
    suspend fun saveStatus(item: StatusItem): Boolean
    suspend fun getSavedIds(): Set<String>
    suspend fun getLastScanDebug(): StatusScanDebug
    fun observeSavedHistory(): Flow<List<SavedItem>>
}
