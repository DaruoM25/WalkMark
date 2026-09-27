package com.walkmark.app.core.database

import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getInMemoryDatabaseBuilder(): RoomDatabase.Builder<WalkMarkDatabase> {
    return Room.inMemoryDatabaseBuilder { WalkMarkDatabaseConstructor.initialize() }
}
