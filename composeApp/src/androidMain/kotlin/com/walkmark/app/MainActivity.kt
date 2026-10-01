package com.walkmark.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.walkmark.app.presentation.map.AndroidLiveMap

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(mapContent = { state, modifier -> AndroidLiveMap(state, modifier) })
        }
    }
}
