package com.walkmark.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

lateinit var appContext: Context
	actual fun getDatabaseBuilder(): RoomDatabase.Builder<WalkMarkDatabase> {
    val dbFile = appContext.getDatabasePath("walkmark.db")
    return Room.databaseBuilder<WalkMarkDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
