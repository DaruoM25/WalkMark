package com.walkmark.app.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.walkmark.app.core.database.dao.SampleDao
import com.walkmark.app.core.database.dao.WalkDao
import com.walkmark.app.core.database.dao.WalkNoteDao
import com.walkmark.app.core.database.dao.WalkPhotoDao
import com.walkmark.app.core.database.dao.WalkPointDao
import com.walkmark.app.core.database.entity.SampleEntity
import com.walkmark.app.core.database.entity.WalkEntity
import com.walkmark.app.core.database.entity.WalkNoteEntity
import com.walkmark.app.core.database.entity.WalkPhotoEntity
import com.walkmark.app.core.database.entity.WalkPointEntity

/**
 * `exportSchema = true` is the intended end state, but schema JSON export is currently
 * ENVIRONMENT_BLOCKED: the `androidx.room` Gradle plugin fails here because KSP rejects a schema
 * path containing a space in the workspace path, and no validated export mechanism was found.
 * Consequently `composeApp/schemas` is empty and no schema history is committed.
 *
 * The authoritative evidence for this schema version is therefore behavioral:
 * `WalkMigration_1_2_Test` executes MIGRATION_1_2 against a real SQLite connection and asserts the
 * resulting columns match current entity expectations, and `WalkPersistenceIntegrationTest` proves
 * FK/CASCADE and close-reopen durability. See docs/evidence/us-003a/README.md.
 */
@Database(
    entities = [
        SampleEntity::class,
        WalkEntity::class,
        WalkPointEntity::class,
        WalkNoteEntity::class,
        WalkPhotoEntity::class
    ],
    version = 2,
    exportSchema = true
)
@ConstructedBy(WalkMarkDatabaseConstructor::class)
abstract class WalkMarkDatabase : RoomDatabase() {
    abstract fun sampleDao(): SampleDao
    abstract fun walkDao(): WalkDao
    abstract fun walkPointDao(): WalkPointDao
    abstract fun walkNoteDao(): WalkNoteDao
    abstract fun walkPhotoDao(): WalkPhotoDao
}

@OptIn(androidx.room.ExperimentalRoomApi::class)
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object WalkMarkDatabaseConstructor : RoomDatabaseConstructor<WalkMarkDatabase> {
    override fun initialize(): WalkMarkDatabase
}
