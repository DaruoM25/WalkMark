package com.walkmark.app.presentation.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

internal class IosWalkSummaryShareLauncher : WalkSummaryShareLauncher {
    override fun share(text: String): WalkSummaryShareResult {
        val window = UIApplication.sharedApplication.windows
            .filterIsInstance<UIWindow>()
            .firstOrNull { it.isKeyWindow() }
            ?: return WalkSummaryShareResult.NoCompatibleApp
        var presenter: UIViewController = window.rootViewController
            ?: return WalkSummaryShareResult.NoCompatibleApp
        while (presenter.presentedViewController != null) {
            presenter = presenter.presentedViewController ?: break
        }
        return try {
            val controller = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
            controller.popoverPresentationController?.sourceView = presenter.view
            presenter.presentViewController(controller, animated = true, completion = null)
            WalkSummaryShareResult.Shared
        } catch (_: Exception) {
            WalkSummaryShareResult.Failure
        }
    }
}

@Composable
actual fun rememberWalkSummaryShareLauncher(): WalkSummaryShareLauncher =
    remember { IosWalkSummaryShareLauncher() }
