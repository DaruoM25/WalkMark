package com.walkmark.app.core.database

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WalkMigration_2_3_Test {

    @Test
    fun migrationConstantsMatchExpectedVersions() {
        assertEquals(2, WalkMarkMigrations.VERSION_2)
        assertEquals(3, WalkMarkMigrations.VERSION_3)
        assertEquals(2, WalkMarkMigrations.MIGRATION_2_3.startVersion)
        assertEquals(3, WalkMarkMigrations.MIGRATION_2_3.endVersion)
    }
}
