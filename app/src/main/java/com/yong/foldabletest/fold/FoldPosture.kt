package com.yong.foldabletest.fold

import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowLayoutInfo

/**
 * Device postures
 */
enum class FoldPosture {
    // No Fold: Cover screen / Non-foldable device / Window mode etc
    NoFold,
    // Flat: Fully unfolded state of Foldable device
    Flat,
    // Tabletop: Half-folded state like as laptop
    Tabletop,
    // Book: Half-folded, and orientated as vertical as book
    Book,
}

fun WindowLayoutInfo.foldingFeature(): FoldingFeature? =
    displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()

fun FoldingFeature?.toFoldPosture(): FoldPosture = when {
    this == null -> FoldPosture.NoFold
    state == FoldingFeature.State.FLAT -> FoldPosture.Flat
    orientation == FoldingFeature.Orientation.HORIZONTAL -> FoldPosture.Tabletop
    else -> FoldPosture.Book
}
