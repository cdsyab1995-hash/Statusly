package com.yourcompany.statusvault.data.repository

import android.content.Context
import android.net.Uri
import com.yourcompany.statusvault.data.datasource.file.StatusDirectoryScanner
import com.yourcompany.statusvault.data.local.dao.DirectoryGrantDao
import com.yourcompany.statusvault.data.local.toDomain
import com.yourcompany.statusvault.data.local.toEntity
import com.yourcompany.statusvault.domain.model.DirectoryGrant
import com.yourcompany.statusvault.domain.model.SourceApp
import com.yourcompany.statusvault.domain.repository.DirectoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DirectoryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val directoryGrantDao: DirectoryGrantDao,
    private val statusDirectoryScanner: StatusDirectoryScanner,
) : DirectoryRepository {
    override suspend fun getGrant(sourceApp: SourceApp): DirectoryGrant? {
        return directoryGrantDao.getBySourceApp(sourceApp.name)?.toDomain()
    }

    override suspend fun saveGrant(grant: DirectoryGrant) {
        directoryGrantDao.insert(grant.toEntity())
    }

    override suspend fun validateGrant(sourceApp: SourceApp): Boolean {
        return runCatching {
            val grant = directoryGrantDao.getBySourceApp(sourceApp.name)?.toDomain() ?: return@runCatching false
            if (!grant.isValid) return@runCatching false

            statusDirectoryScanner.hasUsableStatusDirectory(
                context = context,
                treeUri = Uri.parse(grant.treeUri),
                sourceApp = sourceApp,
            )
        }.getOrDefault(false)
    }
}
