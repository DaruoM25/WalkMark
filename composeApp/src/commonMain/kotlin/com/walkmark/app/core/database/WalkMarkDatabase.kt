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
 * Room schema JSON is exported to `composeApp/schemas` via the `androidx.room` Gradle plugin
 * (`room { schemaDirectory("schemas") }`) and committed to the repository. The exported schemas
 * are the source of truth for schema-version history; `WalkMigration_1_2_Test` additionally
 * executes MIGRATION_1_2 against a real SQLite connection to prove data-preservation and
 * FK/CASCADE behavior.
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
