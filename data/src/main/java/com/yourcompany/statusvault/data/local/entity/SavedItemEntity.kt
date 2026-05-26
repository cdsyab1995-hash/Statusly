package com.yourcompany.statusvault.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_items")
data class SavedItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "status_id")
    val statusId: String,
    @ColumnInfo(name = "source_app")
    val sourceApp: String,
    @ColumnInfo(name = "media_type")
    val mediaType: String,
    @ColumnInfo(name = "saved_uri")
    val savedUri: String,
    @ColumnInfo(name = "saved_at")
    val savedAt: Long,
    @ColumnInfo(name = "file_name")
    val fileName: String,
)
