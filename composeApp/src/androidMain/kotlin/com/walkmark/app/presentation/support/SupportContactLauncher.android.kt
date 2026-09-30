package com.walkmark.app.presentation.support

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

internal fun createSupportContactIntent(request: SupportContactRequest): Intent {
    val uri = Uri.parse(
        "mailto:${Uri.encode(request.recipient)}" +
            "?subject=${Uri.encode(request.subject)}&body=${Uri.encode(request.body)}"
    )
    return Intent(Intent.ACTION_SENDTO, uri)
}

internal class AndroidSupportContactLauncher(
    private val context: Context
) : SupportContactLauncher {
    override fun launch(
        request: SupportContactRequest,
        onResult: (SupportContactResult) -> Unit
    ) {
        val intent = createSupportContactIntent(request)
        if (intent.resolveActivity(context.packageManager) == null) {
            onResult(SupportContactResult.NoCompatibleApp)
            return
        }
        runCatching { context.startActivity(intent) }
            .onSuccess { onResult(SupportContactResult.Success) }
            .onFailure { onResult(SupportContactResult.Failure) }
    }
}

@Composable
actual fun rememberSupportContactLauncher(): SupportContactLauncher {
    val context = LocalContext.current
    return remember(context) { AndroidSupportContactLauncher(context) }
}
