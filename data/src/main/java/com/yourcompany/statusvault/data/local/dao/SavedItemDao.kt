package com.yourcompany.statusvault.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourcompany.statusvault.data.local.entity.SavedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SavedItemEntity): Long

    @Query("SELECT * FROM saved_items ORDER BY saved_at DESC")
    fun observeAll(): Flow<List<SavedItemEntity>>

    @Query("SELECT status_id FROM saved_items")
    suspend fun getSavedStatusIds(): List<String>
}
