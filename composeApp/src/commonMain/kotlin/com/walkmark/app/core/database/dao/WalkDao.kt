package com.walkmark.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.walkmark.app.core.database.entity.WalkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(walk: WalkEntity)

    @Update
    suspend fun update(walk: WalkEntity)

    @Query("SELECT * FROM walks WHERE id = :walkId LIMIT 1")
    suspend fun getById(walkId: String): WalkEntity?

    @Query("SELECT * FROM walks WHERE id = :walkId LIMIT 1")
    fun observeById(walkId: String): Flow<WalkEntity?>

    @Query("SELECT * FROM walks WHERE status = :status ORDER BY startTimeEpochMs DESC LIMIT 1")
    suspend fun getFirstByStatus(status: String): WalkEntity?

    @Query("SELECT * FROM walks WHERE status = :status ORDER BY startTimeEpochMs DESC LIMIT 1")
    fun observeFirstByStatus(status: String): Flow<WalkEntity?>

    @Query("SELECT * FROM walks ORDER BY startTimeEpochMs DESC")
    fun observeAll(): Flow<List<WalkEntity>>

    @Query("SELECT * FROM walks ORDER BY startTimeEpochMs DESC")
    suspend fun getAll(): List<WalkEntity>

    @Query("SELECT COUNT(id) FROM walks")
    suspend fun count(): Int

    @Delete
    suspend fun delete(walk: WalkEntity)

    @Query("DELETE FROM walks WHERE id = :walkId")
    suspend fun deleteById(walkId: String)
}
