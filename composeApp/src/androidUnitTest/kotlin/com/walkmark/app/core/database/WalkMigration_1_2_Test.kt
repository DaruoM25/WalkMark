package com.walkmark.app.core.database

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Executes MIGRATION_1_2 against a real SQLite connection seeded with the v1 schema.
 *
 * Schema-JSON driven verification (Room's `MigrationTestHelper`) is not usable here because the
 * Room Gradle plugin's `schemaDirectory` DSL injects a KSP processor argument containing the
 * project path, and KSP requires arguments in `\S+=\S+` format. Any workspace path containing a
 * space fails the build with "Processor arguments not in the format \S+=\S+". `exportSchema`
 * therefore stays disabled on WalkMarkDatabase.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WalkMigration_1_2_Test {

    private fun withSeededVersion1(block: (SQLiteConnection) -> Unit) {
        val driver = AndroidSQLiteDriver()
        val connection = driver.open(":memory:")
        try {
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
            block(connection)
        } finally {
            connection.close()
        }
    }

    private fun tableNames(connection: SQLiteConnection): List<String?> =
        connection.queryFirstColumn("SELECT name FROM sqlite_master WHERE type='table'")

    /**
     * `PRAGMA table_info` returns (cid, name, type, notnull, dflt_value, pk), so the column
     * names live at index 1. [queryFirstColumn] is hardcoded to index 0 and therefore returns
     * `cid` here, so the statement is stepped directly instead of through that helper.
     */
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
    fun migrationCreatesAllFourWalkTablesAndKeepsSamples() = withSeededVersion1 { connection ->
        WalkMarkMigrations.MIGRATION_1_2.migrate(connection)

        val tables = tableNames(connection)
        assertTrue("samples" in tables, "samples table must be preserved")
        assertTrue("walks" in tables, "walks table must be created, was: $tables")
        assertTrue("walk_points" in tables)
        assertTrue("walk_notes" in tables)
        assertTrue("walk_photos" in tables)
    }

    @Test
    fun migrationPreservesExistingSampleRows() = withSeededVersion1 { connection ->
        WalkMarkMigrations.MIGRATION_1_2.migrate(connection)

        assertEquals(listOf("Forest Trail"), connection.queryFirstColumn("SELECT title FROM samples"))
        assertEquals(listOf("2000"), connection.queryFirstColumn("SELECT createdAt FROM samples"))
    }

    @Test
    fun migrationCreatesForeignKeyIndices() = withSeededVersion1 { connection ->
        WalkMarkMigrations.MIGRATION_1_2.migrate(connection)

        val indices = connection.queryFirstColumn(
            "SELECT name FROM sqlite_master WHERE type='index' AND name LIKE 'index_%'"
        )
        assertTrue("index_walk_points_walkId" in indices, "was: $indices")
        assertTrue("index_walk_notes_walkId" in indices, "was: $indices")
        assertTrue("index_walk_photos_walkId" in indices, "was: $indices")
    }

    @Test
    fun migrationCreatesCascadingForeignKeysToWalks() = withSeededVersion1 { connection ->
        WalkMarkMigrations.MIGRATION_1_2.migrate(connection)

        for (table in listOf("walk_points", "walk_notes", "walk_photos")) {
            val foreignKeys = connection.queryFirstColumn("PRAGMA foreign_key_list(`$table`)")
            assertTrue(foreignKeys.isNotEmpty(), "$table must declare a foreign key")

            val sql = connection.queryFirstColumn(
                "SELECT sql FROM sqlite_master WHERE type='table' AND name='$table'"
            ).orEmpty().joinToString(" ")

            assertTrue(
                "ON DELETE CASCADE" in sql,
                "$table foreign key must cascade on delete, was: $sql"
            )
        }
    }

    @Test
    fun migratedWalkColumnsMatchRoomExpectations() = withSeededVersion1 { connection ->
        WalkMarkMigrations.MIGRATION_1_2.migrate(connection)

        val names = columnNames(connection, "walks")
        assertEquals(
            listOf(
                "id", "title", "summary", "startTimeEpochMs", "endTimeEpochMs",
                "totalDistanceMeters", "durationSeconds", "status"
            ),
            names
        )
    }

    @Test
    fun newTablesStartEmptyAfterMigration() = withSeededVersion1 { connection ->
        WalkMarkMigrations.MIGRATION_1_2.migrate(connection)

        assertTrue(connection.queryFirstColumn("SELECT id FROM walks").isEmpty())
        assertTrue(connection.queryFirstColumn("SELECT id FROM walk_points").isEmpty())
        assertTrue(connection.queryFirstColumn("SELECT id FROM walk_notes").isEmpty())
        assertTrue(connection.queryFirstColumn("SELECT id FROM walk_photos").isEmpty())
    }

    @Test
    fun migrationIsIdempotentOnExistingConnection() = withSeededVersion1 { connection ->
        WalkMarkMigrations.MIGRATION_1_2.migrate(connection)
        WalkMarkMigrations.MIGRATION_1_2.migrate(connection)

        assertEquals(1, connection.queryFirstColumn("SELECT title FROM samples").size)
    }
}
