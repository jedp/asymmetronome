package com.jedparsons.metronome.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Utility to convert UI drag offset to single unit increments / decrements.
 */
class HorizontalDragHandler(
  private val dpPerUnit: Int = 35,
  private val incrementValueBy: (Int) -> Unit
) {

  private var currentOffset: Dp = 0.dp
  private var lastOffset: Dp = 0.dp

  fun onDragStart(offset: Offset) {
    currentOffset = 0.dp
    lastOffset = 0.dp
  }

  fun onDrag(offset: Offset) {
    currentOffset += offset.x.dp

    if (abs(currentOffset.value - lastOffset.value) > dpPerUnit) {
      incrementValueBy(if (currentOffset < lastOffset) -1 else 1)
      lastOffset = currentOffset
    }
  }
}