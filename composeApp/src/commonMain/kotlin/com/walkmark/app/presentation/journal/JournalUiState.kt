package com.walkmark.app.presentation.journal

import com.walkmark.app.core.model.SampleItem

data class JournalUiState(
    val isLoading: Boolean = false,
    val items: List<SampleItem> = emptyList(),
    val errorMessage: String? = null
)