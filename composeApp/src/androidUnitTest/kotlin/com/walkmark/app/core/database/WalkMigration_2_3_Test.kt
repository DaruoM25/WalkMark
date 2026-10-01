package com.walkmark.app.core.database

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WalkMigration_2_3_Test {

    private fun withSeededVersion2(block: (SQLiteConnection) -> Unit) {
        val driver = AndroidSQLiteDriver()
        val connection = driver.open(":memory:")
        try {
            // Seed v1
            connection.execute(
                """
                CREATE TABLE IF NOT EXISTS `samples` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            connection.execute("INSERT INTO `samples` (`title`, `createdAt`) VALUES ('Forest Trail', 2000)")
            connection.execute("PRAGMA user_version = 1")

            // Migrate to v2
            WalkMarkMigrations.MIGRATION_1_2.migrate(connection)
            connection.execute("PRAGMA user_version = 2")

            // Seed v2 data
            connection.execute(
                """
                INSERT INTO `walks` (`id`, `title`, `summary`, `startTimeEpochMs`, `endTimeEpochMs`, `totalDistanceMeters`, `durationSeconds`, `status`)
                VALUES ('walk-100', 'Morning Walk', 'Sunny day', 1000, 2000, 1500.0, 1000, 'COMPLETED')
                """.trimIndent()
            )
            connection.execute(
                """
                INSERT INTO `walk_points` (`id`, `walkId`, `seq`, `latitude`, `longitude`, `altitude`, `timestampEpochMs`, `accuracyMeters`)
                VALUES ('pt-1', 'walk-100', 0, 48.8566, 2.3522, 35.0, 1050, 4.5)
                """.trimIndent()
            )
            connection.execute(
                """
                INSERT INTO `walk_notes` (`id`, `walkId`, `text`, `latitude`, `longitude`, `createdAtEpochMs`)
                VALUES ('note-1', 'walk-100', 'Nice bench', 48.8567, 2.3523, 1100)
                """.trimIndent()
            )

            block(connection)
        } finally {
            connection.close()
        }
    }

    private fun tableNames(connection: SQLiteConnection): List<String?> =
        connection.queryFirstColumn("SELECT name FROM sqlite_master WHERE type='table'")

    private fun columnNames(connection: SQLiteConnection, table: String): List<String> {
        val statement = connection.prepare("PRAGMA table_info(`$table`)")
        try {
            val names = mutableListOf<String>()
            while (statement.step()) {
                names += statement.getText(1)
            }
            return names
        } finally {
            statement.close()
        }
    }

    @Test
    fun migrationConstantsMatchExpectedVersions() {
        assertEquals(2, WalkMarkMigrations.VERSION_2)
        assertEquals(3, WalkMarkMigrations.VERSION_3)
        assertEquals(2, WalkMarkMigrations.MIGRATION_2_3.startVersion)
        assertEquals(3, WalkMarkMigrations.MIGRATION_2_3.endVersion)
    }

    @Test
    fun migrationIsIncludedInCanonicalMigrations() {
        assertTrue(WalkMarkMigrations.ALL_MIGRATIONS.contains(WalkMarkMigrations.MIGRATION_2_3))
        assertEquals(2, WalkMarkMigrations.ALL_MIGRATIONS.size)
        assertEquals(WalkMarkMigrations.MIGRATION_1_2, WalkMarkMigrations.ALL_MIGRATIONS[0])
        assertEquals(WalkMarkMigrations.MIGRATION_2_3, WalkMarkMigrations.ALL_MIGRATIONS[1])
    }

    @Test
    fun migrationCreatesSyncOperationsTableAndPreservesV2Tables() = withSeededVersion2 { connection ->
        WalkMarkMigrations.MIGRATION_2_3.migrate(connection)

        val tables = tableNames(connection)
        assertTrue("samples" in tables, "samples table must be preserved")
        assertTrue("walks" in tables, "walks table must be preserved")
        assertTrue("walk_points" in tables, "walk_points table must be preserved")
        assertTrue("walk_notes" in tables, "walk_notes table must be preserved")
        assertTrue("walk_photos" in tables, "walk_photos table must be preserved")
        assertTrue("sync_operations" in tables, "sync_operations table must be created")
    }

    @Test
    fun migrationPreservesExistingV2Data() = withSeededVersion2 { connection ->
        WalkMarkMigrations.MIGRATION_2_3.migrate(connection)

        assertEquals(listOf("Morning Walk"), connection.queryFirstColumn("SELECT title FROM walks WHERE id = 'walk-100'"))
        assertEquals(listOf("Nice bench"), connection.queryFirstColumn("SELECT text FROM walk_notes WHERE id = 'note-1'"))
        assertEquals(listOf("Forest Trail"), connection.queryFirstColumn("SELECT title FROM samples"))
    }

    @Test
    fun migrationCreatesExpectedIndices() = withSeededVersion2 { connection ->
        WalkMarkMigrations.MIGRATION_2_3.migrate(connection)

        val indices = connection.queryFirstColumn(
            "SELECT name FROM sqlite_master WHERE type='index' AND name LIKE 'index_sync_operations_%'"
        )
        assertTrue("index_sync_operations_entity_unique" in indices, "was: $indices")
        assertTrue("index_sync_operations_walkId" in indices, "was: $indices")
        assertTrue("index_sync_operations_order" in indices, "was: $indices")
    }

    @Test
    fun migratedSyncOperationsColumnsMatchRoomExpectations() = withSeededVersion2 { connection ->
        WalkMarkMigrations.MIGRATION_2_3.migrate(connection)

        val names = columnNames(connection, "sync_operations")
        assertEquals(
            listOf(
                "operationId", "entityType", "entityId", "walkId",
                "operationType", "createdAtEpochMs", "attemptCount", "lastAttemptEpochMs"
            ),
            names
        )
    }

    @Test
    fun newSyncOperationsTableStartsEmptyAfterMigration() = withSeededVersion2 { connection ->
        WalkMarkMigrations.MIGRATION_2_3.migrate(connection)

        assertTrue(connection.queryFirstColumn("SELECT operationId FROM sync_operations").isEmpty())
    }

    @Test
    fun migrationIsIdempotentOnExistingConnection() = withSeededVersion2 { connection ->
        WalkMarkMigrations.MIGRATION_2_3.migrate(connection)
        WalkMarkMigrations.MIGRATION_2_3.migrate(connection)

        assertEquals(1, connection.queryFirstColumn("SELECT title FROM walks").size)
    }
}
