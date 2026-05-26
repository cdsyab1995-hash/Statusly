package com.yourcompany.statusvault.data.repository

import android.content.Context
import android.net.Uri
import com.yourcompany.statusvault.data.datasource.file.MediaStoreSaver
import com.yourcompany.statusvault.data.datasource.file.StatusDirectoryScanner
import com.yourcompany.statusvault.data.local.dao.DirectoryGrantDao
import com.yourcompany.statusvault.data.local.dao.SavedItemDao
import com.yourcompany.statusvault.data.local.entity.SavedItemEntity
import com.yourcompany.statusvault.data.local.toDomain
import com.yourcompany.statusvault.domain.model.SavedItem
import com.yourcompany.statusvault.domain.model.SourceApp
import com.yourcompany.statusvault.domain.model.StatusItem
import com.yourcompany.statusvault.domain.model.StatusScanDebug
import com.yourcompany.statusvault.domain.repository.StatusRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StatusRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedItemDao: SavedItemDao,
    private val directoryGrantDao: DirectoryGrantDao,
    private val statusDirectoryScanner: StatusDirectoryScanner,
    private val mediaStoreSaver: MediaStoreSaver,
) : StatusRepository {

    private var lastScanDebug: StatusScanDebug = StatusScanDebug()

    override suspend fun scanStatuses(sourceApp: SourceApp): List<StatusItem> {
        return runCatching {
            val grant = directoryGrantDao.getBySourceApp(sourceApp.name) ?: return@runCatching emptyList()
            val treeUri = Uri.parse(grant.treeUri)
            val savedIds = getSavedIds()
            val result = statusDirectoryScanner.scan(
                context = context,
                treeUri = treeUri,
                sourceApp = sourceApp,
                savedIds = savedIds,
            )
            lastScanDebug = result.debug
            result.items
        }.getOrElse {
            lastScanDebug = StatusScanDebug(
                grantedPath = null,
                sampleNames = listOf("scan_exception:${it::class.java.simpleName}"),
            )
            emptyList()
        }
    }

    override suspend fun saveStatus(item: StatusItem): Boolean {
        val savedUri = mediaStoreSaver.saveToGallery(
            context = context,
            sourceUri = item.sourceUri,
            displayName = item.fileName,
            mimeType = item.mimeType,
        )
        if (savedUri == null) return false

        savedItemDao.insert(
            SavedItemEntity(
                statusId = item.id,
                sourceApp = item.sourceApp.name,
                mediaType = item.mediaType.name,
                savedUri = savedUri,
                savedAt = System.currentTimeMillis(),
                fileName = item.fileName,
            ),
        )
        return true
    }

    override suspend fun getSavedIds(): Set<String> {
        return savedItemDao.getSavedStatusIds().toSet()
    }

    override fun observeSavedHistory(): Flow<List<SavedItem>> {
        return savedItemDao.observeAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getLastScanDebug(): StatusScanDebug = lastScanDebug
}
