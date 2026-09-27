package com.walkmark.app.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class SampleItemTest {
    @Test
    fun testSampleItemCreation() {
        val item = SampleItem(id = 1, title = "Morning Walk", createdAt = 1000L)
        assertEquals(1L, item.id)
        assertEquals("Morning Walk", item.title)
        assertEquals(1000L, item.createdAt)
    }
}
