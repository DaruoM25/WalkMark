package com.walkmark.app.core.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

expect fun getDatabaseBuilder(): RoomDatabase.Builder<WalkMarkDatabase>

fun createRoomDatabase(
    builder: RoomDatabase.Builder<WalkMarkDatabase>,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): WalkMarkDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(dispatcher)
        .addMigrations(*WalkMarkMigrations.ALL_MIGRATIONS)
        .build()
}
