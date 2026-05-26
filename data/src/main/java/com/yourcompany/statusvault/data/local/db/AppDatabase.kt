package com.yourcompany.statusvault.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yourcompany.statusvault.data.local.dao.DirectoryGrantDao
import com.yourcompany.statusvault.data.local.dao.SavedItemDao
import com.yourcompany.statusvault.data.local.entity.DirectoryGrantEntity
import com.yourcompany.statusvault.data.local.entity.SavedItemEntity

@Database(
    entities = [
        SavedItemEntity::class,
        DirectoryGrantEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedItemDao(): SavedItemDao
    abstract fun directoryGrantDao(): DirectoryGrantDao
}
