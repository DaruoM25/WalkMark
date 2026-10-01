package com.walkmark.app.presentation.share

import android.app.Activity
import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WalkSummaryShareLauncherTest {
    @Test
    fun shareIntentContainsOnlyPlainTextSummary() {
        val text = "Morning walk\nDistance: 1234 m"
        val intent = createWalkSummaryShareIntent(text)

        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertEquals(text, intent.getStringExtra(Intent.EXTRA_TEXT))
        assertNull(intent.data)
        assertEquals(0, intent.flags)
    }

    @Test
    fun launcherOpensChooserWithPlainTextIntent() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val text = "Morning walk\nDistance: 1234 m"

        assertEquals(WalkSummaryShareResult.Shared, AndroidWalkSummaryShareLauncher(activity).share(text))

        val chooser = shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        val send = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertEquals(Intent.ACTION_SEND, send?.action)
        assertEquals("text/plain", send?.type)
        assertEquals(text, send?.getStringExtra(Intent.EXTRA_TEXT))
    }
}
