package com.walkmark.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.walkmark.app.data.database.entity.WalkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(walk: WalkEntity): Long

    @Query("SELECT * FROM walks ORDER BY startTimeEpochMs DESC")
    fun observeAll(): Flow<List<WalkEntity>>

    @Query("SELECT * FROM walks WHERE id = :id")
    suspend fun getById(id: String): WalkEntity?

    @Query("SELECT COUNT(id) FROM walks")
    suspend fun count(): Int

    @Query("DELETE FROM walks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM walks")
    suspend fun clear()
}
