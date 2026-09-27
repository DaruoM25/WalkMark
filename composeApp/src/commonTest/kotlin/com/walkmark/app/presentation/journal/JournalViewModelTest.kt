package com.walkmark.app.presentation.journal

import app.cash.turbine.test
import com.walkmark.app.core.model.SampleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OOptIn(ExperimentalCoroutinesApi::class)
class JournalViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialUiState() = runTest {
        val viewModel = JournalViewModel()
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(emptyList(), state.items)
            assertEquals(null, state.errorMessage)
        }
    }

    @Test
    fun testLoadItemsTransition() = runTest {
        val viewModel = JournalViewModel()
        val items = listOf(SampleItem(1, "Park Walk", 1000L))
        
        viewModel.loadItems(items)
        assertEquals(items, viewModel.uiState.value.items)
    }

    @Test
    fun testAddItemStateUpdate() = runTest {
        val viewModel = JournalViewModel()
        viewModel.addItem("Sunset Walk")
        
        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals("Sunset Walk", viewModel.uiState.value.items.first().title)
    }
}
