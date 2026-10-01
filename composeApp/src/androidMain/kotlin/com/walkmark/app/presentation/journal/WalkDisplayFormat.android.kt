package com.walkmark.app.presentation.journal

import java.text.DateFormat
import java.util.Date

actual fun formatWalkDateTime(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(epochMillis))
