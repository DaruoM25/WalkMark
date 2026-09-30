package com.walkmark.app

import androidx.compose.ui.window.ComposeUIViewController
import com.walkmark.app.presentation.map.IosLiveMap

fun MainViewController() = ComposeUIViewController {
    App(mapContent = { state, modifier -> IosLiveMap(state, modifier) })
}