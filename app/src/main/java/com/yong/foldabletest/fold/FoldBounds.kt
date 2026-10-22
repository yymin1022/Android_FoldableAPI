package com.yong.foldabletest.fold

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toComposeRect
import androidx.window.layout.FoldingFeature

/**
 * Converts the fold bounds into the local coordinates of a layout.
 * - [FoldingFeature.bounds] is in window coordinates, but layout must consider WindowInsets, not the original window coordinates.
 */
fun FoldingFeature.boundsIn(
    layoutBoundsInWindow: Rect,
): Rect = bounds.toComposeRect().translate(-layoutBoundsInWindow.topLeft)
