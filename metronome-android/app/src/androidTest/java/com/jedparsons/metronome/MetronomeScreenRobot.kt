package com.jedparsons.metronome

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.jedparsons.metronome.ui.MetronomeActivity

class MetronomeScreenRobot(
  private val testRule: AndroidComposeTestRule<ActivityScenarioRule<MetronomeActivity>, MetronomeActivity>
) {

  private val onBpm: SemanticsNodeInteraction
    get() = testRule
      .onNodeWithTag("tag-bpm")
      .onChild()

  private val onTap: SemanticsNodeInteraction
    get() = testRule
      .onNodeWithTag("tag-tap-TAP")

  private val onSubdivisions: SemanticsNodeInteractionCollection
    get() = testRule
      .onAllNodes(hasTestTag("tag-subdivision"))

  private val onPlayButton: SemanticsNodeInteraction
    get() = testRule
      .onNodeWithTag("tag-tap-PLAY")

  private val onStopButton: SemanticsNodeInteraction
    get() = testRule
      .onNodeWithTag("tag-tap-STOP")

  fun seeBpm(bpm: Int) = onBpm
    .assertTextEquals(bpm.toString())

  fun dragBpmLeft(pixels: Int) = onBpm
    .performTouchInput {
      swipeLeft(endX = right - pixels)
    }

  fun dragBpmRight(pixels: Int) = onBpm
    .performTouchInput {
      swipeRight(endX = left + pixels)
    }

  fun tempoTap() = onTap.performClick()

  fun seeSubdivisions(values: List<Int>) {
    onSubdivisions.assertCountEquals(values.size)

    values.forEachIndexed { i, value ->
      onSubdivisions[i]
        .onChild()
        .assertTextEquals(value.toString())
    }
  }

  fun dragSubdivisionRight(
    which: Int,
    pixels: Int
  ) {
    onSubdivisions[which]
      .performTouchInput {
        swipeRight(endX = left + pixels)
      }
  }

  fun dragSubdivisionLeft(
    which: Int,
    pixels: Int
  ) {
    onSubdivisions[which]
      .performTouchInput {
        swipeLeft(endX = right - pixels)
      }

  }

  fun seePlayButton() = onPlayButton.assertExists()

  fun tapPlay() = onPlayButton.performClick()

  fun seeStopButton() = onStopButton.assertExists()

  fun tapStop() = onStopButton.performClick()

}
