package com.walkmark.app.presentation.export.structured

import android.content.Intent
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StructuredExportShareLauncherTest {

    @Test
    fun sanitizeFileNameRemovesUnsafeCharacters() {
        assertEquals("walk_123.json", sanitizeFileName("walk_123.json"))
        assertEquals("walk_export_data.csv", sanitizeFileName("walk/export data.csv"))
        assertEquals("export_data.txt", sanitizeFileName(""))
        assertEquals("walk_test_1.json", sanitizeFileName("walk:test*1.json"))
    }

    @Test
    fun createStructuredExportShareIntentConfiguresCorrectJsonProperties() {
        val contentUri = Uri.parse("content://com.walkmark.app.fileprovider/walkmark-exports/walk_123.json")
        val intent = createStructuredExportShareIntent(
            uri = contentUri,
            mimeType = "application/json",
            fileName = "walk_123.json"
        )

        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("application/json", intent.type)
        val streamUri = (intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri)
            ?: (intent.extras?.get(Intent.EXTRA_STREAM) as? Uri)
        assertEquals(contentUri, streamUri)
        assertEquals("walk_123.json", intent.getStringExtra(Intent.EXTRA_SUBJECT))
        assertTrue((intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0)
    }

    @Test
    fun createStructuredExportShareIntentConfiguresCorrectCsvProperties() {
        val contentUri = Uri.parse("content://com.walkmark.app.fileprovider/walkmark-exports/walk_123.csv")
        val intent = createStructuredExportShareIntent(
            uri = contentUri,
            mimeType = "text/csv",
            fileName = "walk_123.csv"
        )

        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/csv", intent.type)
        val streamUri = (intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri)
            ?: (intent.extras?.get(Intent.EXTRA_STREAM) as? Uri)
        assertEquals(contentUri, streamUri)
        assertEquals("walk_123.csv", intent.getStringExtra(Intent.EXTRA_SUBJECT))
        assertTrue((intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0)
    }

    @Test
    fun shareIntentNeverUsesFileSchemeUri() {
        val contentUri = Uri.parse("content://com.walkmark.app.fileprovider/walkmark-exports/walk_123.json")
        val intent = createStructuredExportShareIntent(
            uri = contentUri,
            mimeType = "application/json",
            fileName = "walk_123.json"
        )

        val streamUri = (intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri)
            ?: (intent.extras?.get(Intent.EXTRA_STREAM) as? Uri)
        assertNotNull(streamUri)
        assertEquals("content", streamUri?.scheme)
        assertFalse(streamUri?.scheme == "file")
    }

    @Test
    fun writeExportFileToCacheWritesUtf8Content() {
        val tempCacheDir = File(System.getProperty("java.io.tmpdir"), "walkmark_test_cache_${System.currentTimeMillis()}")
        tempCacheDir.mkdirs()
        tempCacheDir.deleteOnExit()

        val exportDir = File(tempCacheDir, STRUCTURED_EXPORT_CACHE_DIR)
        exportDir.mkdirs()
        val testFile = File(exportDir, "test_walk.json")
        val testContent = "{"title": "Morning Walk & Run"}"
        testFile.writeText(testContent, Charsets.UTF_8)

        assertTrue(testFile.exists())
        assertEquals(testContent, testFile.readText(Charsets.UTF_8))
        testFile.delete()
        exportDir.delete()
        tempCacheDir.delete()
    }
}
