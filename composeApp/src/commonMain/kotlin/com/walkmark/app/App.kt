package com.walkmark.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.rcompose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.rcompose.runtime.Composable
import androidx.rcompose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.walkmark.app.presentation.journal.JournalScreen
import com.walkmark.app.presentation.theme.WalkMarkTheme

@Composable
fun App() {
    WalkMarkTheme {
        Surface(
            modifier = Modifier.fillMaxSize().testTag("app_root_surface"),
            color = MaterialTheme.colorScheme.background
        ) {
            JournalScreen()
        }
    }
}