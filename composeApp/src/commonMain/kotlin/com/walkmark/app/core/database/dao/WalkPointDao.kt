package com.walkmark.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.walkmark.app.core.database.entity.WalkPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkPointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(points: List<WalkPointEntity>)

    @Query("SELECT * FROM walk_points WHERE walkId = :walkId ORDER BY seq ASC")
    fun observeByWalk(walkId: String): Flow<List<WalkPointEntity>>

    @Query("SELECT * FROM walk_points WHERE walkId = :walkId ORDER BY seq ASC")
    suspend fun getByWalk(walkId: String): List<WalkPointEntity>

    @Query("SELECT MAX(seq) FROM walk_points WHERE walkId = :walkId")
    suspend fun maxSeq(walkId: String): Int?

    @Query("SELECT COUNT(id) FROM walk_points WHERE walkId = :walkId")
    suspend fun countForWalk(walkId: String): Int

    @Query("DELETE FROM walk_points WHERE walkId = :walkId")
    suspend fun deleteForWalk(walkId: String)
}
