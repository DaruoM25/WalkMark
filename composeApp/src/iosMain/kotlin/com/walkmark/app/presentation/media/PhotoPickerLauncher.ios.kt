package com.walkmark.app.presentation.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import platform.Foundation.NSURL
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerConfiguration
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject

private class PhotoPickerDelegate(
    private val onPicked: (String) -> Unit,
    private val onFinish: () -> Unit
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        val itemProvider = (didFinishPicking.firstOrNull() as? PHPickerResult)?.itemProvider
        val typeIdentifier = itemProvider?.registeredTypeIdentifiers?.firstOrNull()

        if (itemProvider == null || typeIdentifier == null ||
            !itemProvider.hasItemConformingToTypeIdentifier(typeIdentifier)
        ) {
            onFinish()
            return
        }

        itemProvider.loadFileRepresentationForTypeIdentifier(typeIdentifier) { url, _ ->
            val path = url?.path
            if (path != null) {
                onPicked(NSURL.fileURLWithPath(path).absoluteString)
            }
            onFinish()
        }
    }
}

private fun keyWindow(): UIWindow? =
    UIApplication.sharedApplication.windows
        .filterIsInstance<UIWindow>()
        .firstOrNull { it.isKeyWindow() }
        ?: UIApplication.sharedApplication.windows.filterIsInstance<UIWindow>().firstOrNull()

private fun presentViewController(controller: UIViewController) {
    var presenter: UIViewController? = keyWindow()?.rootViewController
    while (presenter is UINavigationController) {
        presenter = presenter.viewControllers.firstOrNull()
    }
    presenter?.presentViewController(controller, animated = true, completion = null)
}

@Composable
actual fun rememberPhotoPickerLauncher(onPhotoPicked: (String) -> Unit): () -> Unit {
    val currentOnPhotoPicked by rememberUpdatedState(onPhotoPicked)

    return remember {
        {
            val controller = PHPickerViewController(
                configuration = PHPickerViewControllerConfiguration().apply {
                    selectionLimit = 1
                }
            )
            controller.setDelegate(
                PhotoPickerDelegate(
                    onPicked = { urlString -> currentOnPhotoPicked(urlString) },
                    onFinish = { controller.dismissViewControllerAnimated(true) {} }
                )
            )
            presentViewController(controller)
        }
    }
}
