package com.walkmark.app.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun getDatabaseBuilder(): RoomDatabase.Builder<WalkMarkDatabase> {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL= null,
        create = false,
        error = null
    )
    val path = requireNotNull(documentDirectory?.path) + "/walkmark.db"
    return Room.databaseBuilder<WalkMarkDatabase>(
        name = path
    )
}
