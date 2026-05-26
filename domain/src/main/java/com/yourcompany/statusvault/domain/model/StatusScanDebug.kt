package com.yourcompany.statusvault.domain.model

data class StatusScanDebug(
    val grantedPath: String? = null,
    val resolvedStatusPaths: List<String> = emptyList(),
    val totalEntries: Int = 0,
    val fileEntries: Int = 0,
    val imageCandidates: Int = 0,
    val videoCandidates: Int = 0,
    val filteredOutEntries: Int = 0,
    val sampleNames: List<String> = emptyList(),
)
