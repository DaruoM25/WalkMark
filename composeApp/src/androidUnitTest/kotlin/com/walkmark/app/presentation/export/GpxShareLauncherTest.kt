package com.walkmark.app.presentation.export

import android.content.Intent
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GpxShareLauncherTest {
    @Test
    fun intentUsesActionSendWithGpxMimeTypeAndReadGrant() {
        val uri = Uri.parse("content://com.walkmark.app.fileprovider/walkmark-gpx/walkmark-test.gpx")
        val intent = createGpxShareIntent(uri, "walkmark-test.gpx")

        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals(GPX_MIME_TYPE, intent.type)
        assertEquals(uri, intent.getParcelableExtra(Intent.EXTRA_STREAM))
        assertEquals("walkmark-test.gpx", intent.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals(
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
            intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}
