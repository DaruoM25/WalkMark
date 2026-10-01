package com.walkmark.app.presentation.adaptive

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Common presentation width classification based on standard Material 3 responsive thresholds.
 */
enum class AdaptiveWidthClass {
    Compact,   // < 600.dp (phones, fold external display)
    Medium,    // 600.dp .. 839.dp (small tablets, large foldables, landscape phones)
    Expanded   // >= 840.dp (tablets, iPad, fold unfolded, large screens)
}

/**
 * Platform posture abstraction for folding / tabletop postures.
 */
sealed class DevicePosture {
    data object Normal : DevicePosture()
    data object Book : DevicePosture()
    data object TableTop : DevicePosture()
    data class SeparatingHinge(val isVertical: Boolean, val boundsPx: Long = 0L) : DevicePosture()
}

/**
 * Resolved layout mode defining how the presentation shell composes primary & secondary regions.
 */
enum class AdaptiveLayoutMode {
    CompactSinglePane,
    CompactFlex,
    MediumSinglePane,
    ExpandedTwoPane
}

/**
 * Immutable UI state representing the current adaptive window & posture configuration.
 */
data class AdaptiveUiState(
    val widthClass: AdaptiveWidthClass,
    val posture: DevicePosture = DevicePosture.Normal,
    val layoutMode: AdaptiveLayoutMode = AdaptiveLayoutMode.CompactSinglePane,
    val isSecondaryPanelVisible: Boolean = false
)

object AdaptiveLayoutResolver {

    fun classifyWidth(width: Dp): AdaptiveWidthClass {
        return when {
            width < 600.dp -> AdaptiveWidthClass.Compact
            width < 840.dp -> AdaptiveWidthClass.Medium
            else -> AdaptiveWidthClass.Expanded
        }
    }

    fun resolveLayoutMode(
        widthClass: AdaptiveWidthClass,
        posture: DevicePosture = DevicePosture.Normal
    ): AdaptiveLayoutMode {
        return when {
            posture is DevicePosture.TableTop -> AdaptiveLayoutMode.CompactFlex
            widthClass == AdaptiveWidthClass.Expanded -> AdaptiveLayoutMode.ExpandedTwoPane
            widthClass == AdaptiveWidthClass.Medium -> AdaptiveLayoutMode.MediumSinglePane
            else -> AdaptiveLayoutMode.CompactSinglePane
        }
    }

    /**
     * Calculates two-pane width distribution safely respecting minimum constraints.
     * Default target: approx 70% primary, 30% secondary.
     */
    fun calculateTwoPaneWidths(
        totalWidth: Dp,
        minPrimaryWidth: Dp = 360.dp,
        minSecondaryWidth: Dp = 280.dp,
        primaryWeight: Float = 0.70f
    ): Pair<Dp, Dp> {
        val targetPrimary = totalWidth * primaryWeight
        val targetSecondary = totalWidth - targetPrimary

        if (targetSecondary < minSecondaryWidth) {
            val adjustedSecondary = minSecondaryWidth
            val adjustedPrimary = (totalWidth - adjustedSecondary).coerceAtLeast(minPrimaryWidth)
            return Pair(adjustedPrimary, adjustedSecondary)
        }

        if (targetPrimary < minPrimaryWidth) {
            val adjustedPrimary = minPrimaryWidth
            val adjustedSecondary = (totalWidth - adjustedPrimary).coerceAtLeast(minSecondaryWidth)
            return Pair(adjustedPrimary, adjustedSecondary)
        }

        return Pair(targetPrimary, targetSecondary)
    }
}

