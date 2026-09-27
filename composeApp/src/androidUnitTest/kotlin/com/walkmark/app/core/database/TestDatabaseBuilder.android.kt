package com.walkmark.app.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import org.robolectric.RuntimeEnvironment

actual fun getInMemoryDatabaseBuilder(): RoomDatabase.Builder<WalkMarkDatabase> {
    return Room.inMemoryDatabaseBuilder(
        RuntimeEnvironment.getApplication(),
        WalkMarkDatabase::class.java
    )
}
