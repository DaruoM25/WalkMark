package com.walkmark.app.presentation.share

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

internal fun createWalkSummaryShareIntent(text: String): Intent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, text)
}

internal class AndroidWalkSummaryShareLauncher(private val context: Context) : WalkSummaryShareLauncher {
    override fun share(text: String): WalkSummaryShareResult = try {
        context.startActivity(Intent.createChooser(createWalkSummaryShareIntent(text), "Share walk summary"))
        WalkSummaryShareResult.Shared
    } catch (_: ActivityNotFoundException) {
        WalkSummaryShareResult.NoCompatibleApp
    } catch (_: Exception) {
        WalkSummaryShareResult.Failure
    }
}

@Composable
actual fun rememberWalkSummaryShareLauncher(): WalkSummaryShareLauncher {
    val context = LocalContext.current
    return remember(context) { AndroidWalkSummaryShareLauncher(context) }
}
