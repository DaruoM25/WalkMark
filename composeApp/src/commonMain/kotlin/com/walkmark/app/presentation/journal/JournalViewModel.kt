package com.walkmark.app.presentation.journal

import androidx.lifecycle.ViewModel
import com.walkmark.app.core.model.SampleItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class JournalViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    fun loadItems(sampleList: List<SampleItem> = emptyList()) {
        _uiState.update { it.copy(isLoading = true) }
        _uiState.update {
            it.copy(
                isLoading = false,
                items = sampleList,
                errorMessage = null
            )
        }
    }

    fun addItem(title: String) {
        if (title.isBlank()) return
        val newItem = SampleItem(
            id = (uiState.value.items.size + 1).toLong(),
            title = title.trim(),
            createdAt = 1000L
        )
        _uiState.update {
            it.copy(items = it.items + newItem)
        }
    }
}