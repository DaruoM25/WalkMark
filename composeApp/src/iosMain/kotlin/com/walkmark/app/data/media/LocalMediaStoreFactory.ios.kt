package com.walkmark.app.data.media

import com.walkmark.app.domain.repository.LocalMediaStore

actual fun createLocalMediaStore(): LocalMediaStore = IosLocalMediaStore()
