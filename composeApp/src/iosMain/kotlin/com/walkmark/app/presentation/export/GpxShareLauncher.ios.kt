package com.walkmark.app.presentation.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSTemporaryDirectory
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal const val GPX_EXPORT_DIRECTORY = "walkmark-gpx"

/**
 * Writes the GPX document into the app-private temporary directory and presents the
 * system share sheet. No filesystem permission is required; the temporary directory is
 * transient and reclaimable by the system.
 */
internal class IosGpxShareLauncher(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : GpxShareLauncher {

    override fun launch(request: GpxShareRequest, onResult: (GpxShareResult) -> Unit) {
        scope.launch {
            val fileUrl = try {
                writeGpxToTemporaryDirectory(request)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                null
            }

            withContext(Dispatchers.Main) {
                if (fileUrl == null) {
                    onResult(GpxShareResult.Failure)
                    return@withContext
                }
                val rootController = UIApplication.sharedApplication.windows
                    .filterIsInstance<UIWindow>()
                    .firstOrNull { it.isKeyWindow() }
                    ?.rootViewController

                if (rootController == null) {
                    onResult(GpxShareResult.Failure)
                    return@withContext
                }

                val activityController = UIActivityViewController(
                    activityItems = listOf(fileUrl),
                    applicationActivities = null
                )
                rootController.presentViewController(
                    activityController,
                    animated = true,
                    completion = { onResult(GpxShareResult.Shared) }
                )
            }
        }
    }

    private fun writeGpxToTemporaryDirectory(request: GpxShareRequest): Any {
        val fileManager = NSFileManager.defaultManager
        val directory = NSTemporaryDirectory() + GPX_EXPORT_DIRECTORY + "/"

        if (!fileManager.fileExistsAtPath(directory, isDirectory = true)) {
            fileManager.createDirectoryAtPath(
                path = directory,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        }

        val path = directory + request.fileName
        val written = (request.content as NSString).writeToFile(
            path = path,
            atomically = true,
            encoding = NSUTF8StringEncoding
        )
        check(written) { "Unable to write the GPX document" }
        return NSFileManager.defaultManager.URLWithPath(path)
    }
}

@Composable
actual fun rememberGpxShareLauncher(): GpxShareLauncher {
    return remember { IosGpxShareLauncher() }
}
