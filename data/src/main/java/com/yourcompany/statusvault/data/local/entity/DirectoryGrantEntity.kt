package com.yourcompany.statusvault.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "directory_grants")
data class DirectoryGrantEntity(
    @PrimaryKey
    @ColumnInfo(name = "source_app")
    val sourceApp: String,
    @ColumnInfo(name = "tree_uri")
    val treeUri: String,
    @ColumnInfo(name = "is_valid")
    val isValid: Boolean,
    @ColumnInfo(name = "granted_at")
    val grantedAt: Long,
)
