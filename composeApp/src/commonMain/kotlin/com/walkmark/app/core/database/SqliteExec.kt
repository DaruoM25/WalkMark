package com.walkmark.app.core.database

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.use

/**
 * Executes a statement that returns no rows (DDL, PRAGMA, INSERT).
 * [androidx.sqlite.SQLiteStatement] exposes only `step`/`reset`, so rows are drained manually.
 */
fun SQLiteConnection.execute(sql: String) {
    prepare(sql).use { statement ->
        while (statement.step()) {
            // drain any rows the statement produced
        }
    }
}

/** Reads every value of the first column returned by [sql]. */
fun SQLiteConnection.queryFirstColumn(sql: String): List<String?> =
    prepare(sql).use { statement ->
        val values = mutableListOf<String?>()
        while (statement.step()) {
            values += if (statement.isNull(0)) null else statement.getText(0)
        }
        values
    }
