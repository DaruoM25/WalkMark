package com.walkmark.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.walkmark.app.core.database.entity.SampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SampleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sample: SampleEntity): Long

    @Query("SELECT * FROM samples ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SampleEntity>>

    @Query("SELECT COUNT(id) FROM samples")
    suspend fun count(): Int

    @Query("DELETE FROM samples")
    suspend fun clear()
}
