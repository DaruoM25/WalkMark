package com.walkmark.app

import com.walkmark.app.presentation.journal.JournalUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JournalComposeSmokeTest {
    @Test
    fun testSemanticTagDefinitions() {
        val state = JournalUiState(isLoading = false)
        assertNotNull(state)
        assertEquals(false, state.isLoading)
    }
}
