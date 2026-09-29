package com.walkmark.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.walkmark.app.core.database.entity.WalkNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkNoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: WalkNoteEntity)

    @Query("SELECT * FROM walk_notes WHERE walkId = :walkId ORDER BY createdAtEpochMs ASC")
    fun observeByWalk(walkId: String): Flow<List<WalkNoteEntity>>

    @Query("SELECT * FROM walk_notes WHERE walkId = :walkId ORDER BY createdAtEpochMs ASC")
    suspend fun getByWalk(walkId: String): List<WalkNoteEntity>

    @Query("SELECT COUNT(id) FROM walk_notes WHERE walkId = :walkId")
    suspend fun countForWalk(walkId: String): Int

    @Query("DELETE FROM walk_notes WHERE walkId = :walkId")
    suspend fun deleteForWalk(walkId: String)
}
