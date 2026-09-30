package com.walkmark.app.presentation.support

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem
import platform.UIKit.UIApplication

internal class IosSupportContactLauncher : SupportContactLauncher {
    override fun launch(
        request: SupportContactRequest,
        onResult: (SupportContactResult) -> Unit
    ) {
        val url = NSURLComponents().apply {
            scheme = "mailto"
            path = request.recipient
            queryItems = listOf(
                NSURLQueryItem(name = "subject", value = request.subject),
                NSURLQueryItem(name = "body", value = request.body)
            )
        }.URL

        if (url == null || !UIApplication.sharedApplication.canOpenURL(url)) {
            onResult(SupportContactResult.NoCompatibleApp)
            return
        }

        UIApplication.sharedApplication.openURL(
            url,
            options = emptyMap<Any?, Any>(),
            completionHandler = { opened ->
                onResult(if (opened) SupportContactResult.Success else SupportContactResult.Failure)
            }
        )
    }
}

@Composable
actual fun rememberSupportContactLauncher(): SupportContactLauncher =
    remember { IosSupportContactLauncher() }
