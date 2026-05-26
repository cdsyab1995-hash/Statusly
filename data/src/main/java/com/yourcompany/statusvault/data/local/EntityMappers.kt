package com.yourcompany.statusvault.data.local

import com.yourcompany.statusvault.data.local.entity.DirectoryGrantEntity
import com.yourcompany.statusvault.data.local.entity.SavedItemEntity
import com.yourcompany.statusvault.domain.model.DirectoryGrant
import com.yourcompany.statusvault.domain.model.MediaType
import com.yourcompany.statusvault.domain.model.SavedItem
import com.yourcompany.statusvault.domain.model.SourceApp

fun DirectoryGrantEntity.toDomain(): DirectoryGrant {
    return DirectoryGrant(
        sourceApp = SourceApp.valueOf(sourceApp),
        treeUri = treeUri,
        isValid = isValid,
        grantedAt = grantedAt,
    )
}

fun DirectoryGrant.toEntity(): DirectoryGrantEntity {
    return DirectoryGrantEntity(
        sourceApp = sourceApp.name,
        treeUri = treeUri,
        isValid = isValid,
        grantedAt = grantedAt,
    )
}

fun SavedItemEntity.toDomain(): SavedItem {
    return SavedItem(
        id = id,
        statusId = statusId,
        sourceApp = SourceApp.valueOf(sourceApp),
        mediaType = MediaType.valueOf(mediaType),
        savedUri = savedUri,
        savedAt = savedAt,
    )
}
