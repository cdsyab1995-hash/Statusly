package com.yourcompany.statusvault.domain.model

data class DirectoryGrant(
    val sourceApp: SourceApp,
    val treeUri: String,
    val isValid: Boolean,
    val grantedAt: Long,
)
