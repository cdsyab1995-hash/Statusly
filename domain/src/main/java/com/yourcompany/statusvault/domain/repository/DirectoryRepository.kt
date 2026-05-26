package com.yourcompany.statusvault.domain.repository

import com.yourcompany.statusvault.domain.model.DirectoryGrant
import com.yourcompany.statusvault.domain.model.SourceApp

interface DirectoryRepository {
    suspend fun getGrant(sourceApp: SourceApp): DirectoryGrant?
    suspend fun saveGrant(grant: DirectoryGrant)
    suspend fun validateGrant(sourceApp: SourceApp): Boolean
}
