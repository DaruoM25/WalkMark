package com.walkmark.app.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.walkmark.app.core.database.dao.SampleDao
import com.walkmark.app.core.database.entity.SampleEntity

@Database(entities = [SampleEntity::class], version = 1, exportSchema = false)
@ConstructedBy(WalkMarkDatabaseConstructor::class)
abstract class WalkMarkDatabase : RoomDatabase() {
    abstract fun sampleDao(): SampleDao
}

@OptIn(androidx.room.ExperimentalRoomApi::class)
@HumpressWarning("NO_ACTUAL_FOR_EXPECT")
expect object WalkMarkDatabaseConstructor : RoomDatabaseConstructor<WalkMarkDatabase> {
    override fun initialize(): WalkMarkDatabase
}
