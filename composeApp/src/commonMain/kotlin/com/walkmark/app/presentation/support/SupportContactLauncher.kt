package com.walkmark.app.presentation.support

import androidx.compose.runtime.Composable

interface SupportContactLauncher {
    fun launch(
        request: SupportContactRequest,
        onResult: (SupportContactResult) -> Unit
    )
}

@Composable
expect fun rememberSupportContactLauncher(): SupportContactLauncher
