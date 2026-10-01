package com.walkmark.app.presentation.export.structured

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

internal fun sanitizeIosFileName(fileName: String): String {
    val sanitized = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    return if (sanitized.isBlank()) "export_data.txt" else sanitized
}

internal class IosStructuredExportShareLauncher : StructuredExportShareLauncher {

    override fun share(
        fileName: String,
        mimeType: String,
        content: String
    ): StructuredExportShareResult {
        val window = UIApplication.sharedApplication.windows
            .filterIsInstance<UIWindow>()
            .firstOrNull { it.isKeyWindow() }
            ?: return StructuredExportShareResult.Unavailable

        var presenter: UIViewController = window.rootViewController
            ?: return StructuredExportShareResult.Unavailable

        while (presenter.presentedViewController != null) {
            presenter = presenter.presentedViewController ?: break
        }

        val safeName = sanitizeIosFileName(fileName)
        val tempDir = NSTemporaryDirectory()
        val filePath = "$tempDir$safeName"

        return try {
            val nsStr = NSString.create(string = content)
            val written = nsStr.writeToFile(filePath, atomically = true, encoding = NSUTF8StringEncoding, error = null)
            if (!written) {
                return StructuredExportShareResult.Failure
            }

            val fileUrl = NSURL.fileURLWithPath(filePath)
            val controller = UIActivityViewController(activityItems = listOf(fileUrl), applicationActivities = null)
            controller.popoverPresentationController?.sourceView = presenter.view
            presenter.presentViewController(controller, animated = true, completion = null)
            StructuredExportShareResult.Presented
        } catch (_: Exception) {
            StructuredExportShareResult.Failure
        }
    }
}

@Composable
actual fun rememberStructuredExportShareLauncher(): StructuredExportShareLauncher =
    remember { IosStructuredExportShareLauncher() }
