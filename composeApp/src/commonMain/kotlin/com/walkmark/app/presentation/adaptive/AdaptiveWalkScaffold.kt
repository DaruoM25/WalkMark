package com.walkmark.app.presentation.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Adaptive presentation scaffold supporting Compact, Medium, Expanded TwoPane, and Flex layouts.
 * Content slots decouple map/journal/controls architecture from the responsive shell.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveWalkScaffold(
    modifier: Modifier = Modifier,
    posture: DevicePosture = DevicePosture.Normal,
    primaryContent: @Composable (AdaptiveUiState) -> Unit,
    secondaryContent: @Composable (AdaptiveUiState) -> Unit,
    controlsContent: (@Composable (AdaptiveUiState) -> Unit)? = null
) {
    var isBottomSheetOpen by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("adaptive_walk_scaffold_root")
            .semantics { contentDescription = "Adaptive Walk Scaffold Root" }
    ) {
        val widthClass = AdaptiveLayoutResolver.classifyWidth(maxWidth)
        val layoutMode = AdaptiveLayoutResolver.resolveLayoutMode(widthClass, posture)
        val adaptiveState = AdaptiveUiState(
            widthClass = widthClass,
            posture = posture,
            layoutMode = layoutMode,
            isSecondaryPanelVisible = layoutMode == AdaptiveLayoutMode.ExpandedTwoPane || isBottomSheetOpen
        )

        when (layoutMode) {
            AdaptiveLayoutMode.ExpandedTwoPane -> {
                val (primaryWidth, secondaryWidth) = AdaptiveLayoutResolver.calculateTwoPaneWidths(maxWidth)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("scaffold_two_pane_layout")
                        .semantics { contentDescription = "Two Pane Expanded Layout" }
                ) {
                    // Primary Pane (~70%)
                    Box(
                        modifier = Modifier
                            .width(primaryWidth)
                            .fillMaxHeight()
                            .testTag("scaffold_primary_pane")
                            .semantics { contentDescription = "Primary Pane Container" }
                    ) {
                        primaryContent(adaptiveState)
                        if (controlsContent != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .testTag("scaffold_controls_container")
                            ) {
                                controlsContent(adaptiveState)
                            }
                        }
                    }

                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight(),
                        color = DividerDefaults.color
                    )

                    // Secondary Pane (~30% Persistent)
                    Box(
                        modifier = Modifier
                            .width(secondaryWidth)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surface)
                            .testTag("scaffold_secondary_pane")
                            .semantics { contentDescription = "Secondary Persistent Pane Container" }
                    ) {
                        secondaryContent(adaptiveState)
                    }
                }
            }

            AdaptiveLayoutMode.CompactFlex -> {
                // TableTop / Flex layout mode: upper visual content, lower controls/content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("scaffold_flex_layout")
                        .semantics { contentDescription = "Flex TableTop Layout" }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("scaffold_flex_upper_pane")
                            .semantics { contentDescription = "Flex Upper Visual Pane" }
                    ) {
                        primaryContent(adaptiveState)
                    }

                    HorizontalDivider(color = DividerDefaults.color)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("scaffold_flex_lower_pane")
                            .semantics { contentDescription = "Flex Lower Controls Pane" }
                    ) {
                        if (controlsContent != null) {
                            controlsContent(adaptiveState)
                        } else {
                            secondaryContent(adaptiveState)
                        }
                    }
                }
            }

            AdaptiveLayoutMode.MediumSinglePane -> {
                // Medium layout with balanced padding
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .testTag("scaffold_medium_single_pane")
                        .semantics { contentDescription = "Medium Single Pane Layout" }
                ) {
                    primaryContent(adaptiveState)
                    if (controlsContent != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .testTag("scaffold_controls_container")
                        ) {
                            controlsContent(adaptiveState)
                        }
                    }
                }
            }

            AdaptiveLayoutMode.CompactSinglePane -> {
                // Compact smartphone single pane layout
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("scaffold_compact_single_pane")
                        .semantics { contentDescription = "Compact Single Pane Layout" }
                ) {
                    primaryContent(adaptiveState)
                    if (controlsContent != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .testTag("scaffold_controls_container")
                        ) {
                            controlsContent(adaptiveState)
                        }
                    }
                }

                if (isBottomSheetOpen) {
                    val sheetState = rememberModalBottomSheetState()
                    ModalBottomSheet(
                        onDismissRequest = { isBottomSheetOpen = false },
                        sheetState = sheetState,
                        modifier = Modifier.testTag("scaffold_bottom_sheet_secondary")
                    ) {
                        secondaryContent(adaptiveState)
                    }
                }
            }
        }
    }
}

