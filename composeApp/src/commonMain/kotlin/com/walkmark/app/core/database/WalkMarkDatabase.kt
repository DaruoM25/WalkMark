package com.walkmark.app.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.walkmark.app.core.database.dao.SampleDao
import com.walkmark.app.core.database.entity.SampleEntity
import com.walkmark.app.data.database.dao.WalkDao
import com.walkmark.app.data.database.entity.WalkEntity

@Database(
    entities = [
        SampleEntity::class,
        WalkEntity::class
    ],
    version = 1,
    exportSchema = false
)
@ConstructedBy(WalkMarkDatabaseConstructor::class)
abstract class WalkMarkDatabase : RoomDatabase() {
    abstract fun sampleDao(): SampleDao
    abstract fun walkDao(): WalkDao
}

@OptIn(androidx.room.ExperimentalRoomApi::class)
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object WalkMarkDatabaseConstructor : RoomDatabaseConstructor<WalkMarkDatabase> {
    override fun initialize(): WalkMarkDatabase
}

