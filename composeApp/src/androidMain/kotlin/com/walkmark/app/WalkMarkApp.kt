package com.walkmark.app

import android.app.Application
import com.walkmark.app.core.database.appContext

class WalkMarkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}
