package com.walkmark.app.presentation.adaptive

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveLayoutTest {

    @Test
    fun testWidthClassificationBoundaries() {
        // Compact (< 600.dp)
        assertEquals(AdaptiveWidthClass.Compact, AdaptiveLayoutResolver.classifyWidth(0.dp))
        assertEquals(AdaptiveWidthClass.Compact, AdaptiveLayoutResolver.classifyWidth(360.dp))
        assertEquals(AdaptiveWidthClass.Compact, AdaptiveLayoutResolver.classifyWidth(599.dp))

        // Medium (600.dp .. 839.dp)
        assertEquals(AdaptiveWidthClass.Medium, AdaptiveLayoutResolver.classifyWidth(600.dp))
        assertEquals(AdaptiveWidthClass.Medium, AdaptiveLayoutResolver.classifyWidth(720.dp))
        assertEquals(AdaptiveWidthClass.Medium, AdaptiveLayoutResolver.classifyWidth(839.dp))

        // Expanded (>= 840.dp)
        assertEquals(AdaptiveWidthClass.Expanded, AdaptiveLayoutResolver.classifyWidth(840.dp))
        assertEquals(AdaptiveWidthClass.Expanded, AdaptiveLayoutResolver.classifyWidth(1024.dp))
        assertEquals(AdaptiveWidthClass.Expanded, AdaptiveLayoutResolver.classifyWidth(1440.dp))
    }

    @Test
    fun testLayoutModeResolutionWithNormalPosture() {
        assertEquals(
            AdaptiveLayoutMode.CompactSinglePane,
            AdaptiveLayoutResolver.resolveLayoutMode(AdaptiveWidthClass.Compact, DevicePosture.Normal)
        )
        assertEquals(
            AdaptiveLayoutMode.MediumSinglePane,
            AdaptiveLayoutResolver.resolveLayoutMode(AdaptiveWidthClass.Medium, DevicePosture.Normal)
        )
        assertEquals(
            AdaptiveLayoutMode.ExpandedTwoPane,
            AdaptiveLayoutResolver.resolveLayoutMode(AdaptiveWidthClass.Expanded, DevicePosture.Normal)
        )
    }

    @Test
    fun testLayoutModeResolutionWithTableTopPosture() {
        assertEquals(
            AdaptiveLayoutMode.CompactFlex,
            AdaptiveLayoutResolver.resolveLayoutMode(AdaptiveWidthClass.Compact, DevicePosture.TableTop)
        )
        assertEquals(
            AdaptiveLayoutMode.CompactFlex,
            AdaptiveLayoutResolver.resolveLayoutMode(AdaptiveWidthClass.Medium, DevicePosture.TableTop)
        )
    }

    @Test
    fun testTwoPaneWidthCalculationsRespectConstraints() {
        val totalWidth = 1000.dp
        val (primary, secondary) = AdaptiveLayoutResolver.calculateTwoPaneWidths(totalWidth)

        assertEquals(700.dp, primary)
        assertEquals(300.dp, secondary)
        assertEquals(totalWidth, primary + secondary)
    }

    @Test
    fun testTwoPaneWidthCalculationsEnforcesMinimumSecondary() {
        val totalWidth = 840.dp
        // 70% of 840 = 588.dp, secondary = 252.dp (< 280.dp min)
        val (primary, secondary) = AdaptiveLayoutResolver.calculateTwoPaneWidths(
            totalWidth = totalWidth,
            minPrimaryWidth = 360.dp,
            minSecondaryWidth = 280.dp
        )

        assertEquals(280.dp, secondary)
        assertEquals(560.dp, primary)
        assertEquals(totalWidth, primary + secondary)
    }
}

