package com.yourcompany.statusvault.domain.model

data class SavedItem(
    val id: Long,
    val statusId: String,
    val sourceApp: SourceApp,
    val mediaType: MediaType,
    val savedUri: String,
    val savedAt: Long,
)
