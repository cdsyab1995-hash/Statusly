package com.yourcompany.statusvault.domain.model

data class StatusItem(
    val id: String,
    val sourceApp: SourceApp,
    val mediaType: MediaType,
    val sourceUri: String,
    val fileName: String,
    val mimeType: String,
    val modifiedAt: Long,
    val isSaved: Boolean = false,
)
