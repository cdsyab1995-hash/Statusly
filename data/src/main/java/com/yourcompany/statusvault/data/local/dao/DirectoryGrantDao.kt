package com.yourcompany.statusvault.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourcompany.statusvault.data.local.entity.DirectoryGrantEntity

@Dao
interface DirectoryGrantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(grant: DirectoryGrantEntity)

    @Query("SELECT * FROM directory_grants WHERE source_app = :sourceApp LIMIT 1")
    suspend fun getBySourceApp(sourceApp: String): DirectoryGrantEntity?
}
