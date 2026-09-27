package com.walkmark.app.integration

import com.revenuecat.purchases.kmp.Purchases
import org.maplibre.compose.map.MapState
import kotlin.test.Test
import kotlin.test.assertNotNull

class DependencyIntegrationTest {

    @Test
    fun testMapLibreAndRevenueCatClassesAreAccessible() {
        // Verify RevenueCat Purchases class is present in classpath
        val purchasesClass = Purchases::class
        assertNotNull(purchasesClass)

        // Verify MapLibre MapState class is present in classpath
        val mapStateClass = MapState::class
        assertNotNull(mapStateClass)
    }
}
