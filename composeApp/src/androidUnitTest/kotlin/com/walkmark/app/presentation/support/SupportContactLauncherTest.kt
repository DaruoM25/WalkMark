package com.walkmark.app.presentation.support

import android.content.Intent
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SupportContactLauncherTest {
    @Test
    fun intentUsesSendToAndEncodedMailtoData() {
        val intent = createSupportContactIntent(
            SupportContactRequest(
                "support+mobile@example.com",
                "WalkMark support request",
                "A problem with spaces & symbols"
            )
        )

        assertEquals(Intent.ACTION_SENDTO, intent.action)
        assertEquals("mailto", intent.data?.scheme)
        assertEquals("support+mobile@example.com", intent.data?.schemeSpecificPart?.substringBefore('?'))
        val encodedQuery = intent.data?.schemeSpecificPart?.substringAfter('?').orEmpty()
        val parameters = encodedQuery.split('&').associate { parameter ->
            val (name, value) = parameter.split('=', limit = 2)
            name to Uri.decode(value)
        }
        assertEquals("WalkMark support request", parameters["subject"])
        assertEquals("A problem with spaces & symbols", parameters["body"])
    }
}
