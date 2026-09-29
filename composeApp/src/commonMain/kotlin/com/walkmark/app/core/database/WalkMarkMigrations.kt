package com.walkmark.app.core.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection

object WalkMarkMigrations {

    const val VERSION_1 = 1
    const val VERSION_2 = 2

    val MIGRATION_1_2 = object : Migration(VERSION_1, VERSION_2) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execute(
                """
                CREATE TABLE IF NOT EXISTS `walks` (
                    `id` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `summary` TEXT,
                    `startTimeEpochMs` INTEGER NOT NULL,
                    `endTimeEpochMs` INTEGER,
                    `totalDistanceMeters` REAL NOT NULL,
                    `durationSeconds` INTEGER NOT NULL,
                    `status` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )

            connection.execute(
                """
                CREATE TABLE IF NOT EXISTS `walk_points` (
                    `id` TEXT NOT NULL,
                    `walkId` TEXT NOT NULL,
                    `seq` INTEGER NOT NULL,
                    `latitude` REAL NOT NULL,
                    `longitude` REAL NOT NULL,
                    `altitude` REAL,
                    `timestampEpochMs` INTEGER NOT NULL,
                    `accuracyMeters` REAL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`walkId`) REFERENCES `walks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execute(
                "CREATE INDEX IF NOT EXISTS `index_walk_points_walkId` ON `walk_points` (`walkId`)"
            )

            connection.execute(
                """
                CREATE TABLE IF NOT EXISTS `walk_notes` (
                    `id` TEXT NOT NULL,
                    `walkId` TEXT NOT NULL,
                    `text` TEXT NOT NULL,
                    `latitude` REAL NOT NULL,
                    `longitude` REAL NOT NULL,
                    `createdAtEpochMs` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`walkId`) REFERENCES `walks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execute(
                "CREATE INDEX IF NOT EXISTS `index_walk_notes_walkId` ON `walk_notes` (`walkId`)"
            )

            connection.execute(
                """
                CREATE TABLE IF NOT EXISTS `walk_photos` (
                    `id` TEXT NOT NULL,
                    `walkId` TEXT NOT NULL,
                    `latitude` REAL NOT NULL,
                    `longitude` REAL NOT NULL,
                    `relativePath` TEXT NOT NULL,
                    `mimeType` TEXT NOT NULL,
                    `byteSize` INTEGER NOT NULL,
                    `createdAtEpochMs` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`walkId`) REFERENCES `walks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execute(
                "CREATE INDEX IF NOT EXISTS `index_walk_photos_walkId` ON `walk_photos` (`walkId`)"
            )
        }
    }
}
