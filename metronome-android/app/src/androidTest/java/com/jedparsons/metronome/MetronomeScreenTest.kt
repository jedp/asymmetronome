package com.jedparsons.metronome

import android.content.Context.MODE_PRIVATE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.jedparsons.metronome.ui.MetronomeActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MetronomeScreenTest {

  @get:Rule
  val testRule = createAndroidComposeRule<MetronomeActivity>()

  private val robot = MetronomeScreenRobot(testRule)

  @Before
  fun setup() {
    getInstrumentation()
      .targetContext
      .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
  }

  @Test
  fun initialState() {
    robot.seeBpm(120)
    robot.seeSubdivisions(listOf(1, 0))
    robot.seePlayButton()
  }

  @Test
  fun tapToSetBpm() {
    robot.tempoTap()
    robot.tempoTap()
    // Robot should have tapped fast enough that we see the max value.
    robot.seeBpm(500)
  }

  @Test
  fun dragToSetBpm() {
    // TODO There should be a "drag until see value" function.
    robot.dragBpmLeft(40)
    robot.seeBpm(119)

    robot.dragBpmRight(50)
    robot.seeBpm(121)
  }

  @Test
  fun statePreservedAfterRotation() {
    // TODO There should be a "drag until see value" function.
    robot.seeBpm(120)
    robot.dragBpmLeft(40)
    robot.seeBpm(119)
    robot.tapPlay()
    robot.seeStopButton()

    testRule.activity.requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE

    // State persists after rotation.
    robot.seeStopButton()
    robot.seeBpm(119)

    testRule.activity.requestedOrientation = SCREEN_ORIENTATION_PORTRAIT

    // State persists after rotation.
    robot.seeStopButton()
    robot.seeBpm(119)

    // Button still works.
    robot.tapStop()
    robot.seePlayButton()
  }

  @Test
  fun testSubdivisions() {
    // TODO There should be a "drag until see value" function.
    robot.dragSubdivisionRight(0, 150)
    robot.seeSubdivisions(listOf(4, 0))

    robot.dragSubdivisionRight(1, 120)
    robot.seeSubdivisions(listOf(4, 2, 0))

    robot.dragSubdivisionRight(2, 150)
    robot.seeSubdivisions(listOf(4, 2, 3, 0))

    robot.dragSubdivisionLeft(1, 120)
    robot.seeSubdivisions(listOf(4, 0))
  }
}