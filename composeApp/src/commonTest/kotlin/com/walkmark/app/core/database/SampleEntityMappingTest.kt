package com.walkmark.app.core.database

import com.walkmark.app.core.database.entity.SampleEntity
import com.walkmark.app.core.model.SampleItem
import kotlin.test.Test
import kotlin.test.assertEquals

class SampleEntityMappingTest {
    @Test
    fun testEntityToDomainMapping() {
        val entity = SampleEntity(id = 42, title = "Forest Trail", createdAt = 2000L)
        val domain = entity.toDomain()

        assertEquals(42L, domain.id)
        assertEquals("Forest Trail", domain.title)
        assertEquals(2000L, domain.createdAt)
    }

    @Test
    fun testDomainToEntityMapping() {
        val domain = SampleItem(id = 10, title = "Beach Walk", createdAt = 3000L)
        val entity = SampleEntity.fromDomain(domain)

        assertEquals(10L, entity.id)
        assertEquals("Beach Walk", entity.title)
        assertEquals(3000L, entity.createdAt)
    }
}
