package com.jedparsons.metronome

import android.app.Activity
import android.content.res.AssetManager
import com.google.common.truth.Truth.assertThat
import com.jedparsons.metronome.player.MetronomeController
import com.jedparsons.metronome.player.MetronomePlayer
import com.jedparsons.metronome.player.toEmphasisPattern
import com.jedparsons.metronome.repo.RhythmRepository
import com.jedparsons.metronome.storage.FakeRhythmStore
import com.jedparsons.metronome.util.FakeMetronomeClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class MetronomeControllerTest {

  private val testScope = TestScope()
  private val context = testScope.coroutineContext
  private val assetManager = mock<AssetManager>()
  private val activity = mock<Activity> {
    on { assets } doReturn (assetManager)
  }
  private val store = FakeRhythmStore(context)
  private val repo = RhythmRepository(store)
  private val clock = FakeMetronomeClock()
  private val player = mock<MetronomePlayer>()

  private val controller = getMetronomeController()

  @Test
  fun `when onActivityStarted then set up metronome`() {
    controller.onActivityStarted(activity)

    verify(player).setUp(assetManager)
  }

  @Test
  fun `when onActivity`() {
    controller.onActivityStopped(activity)

    verify(player).tearDown()
  }

  @Test
  fun `list of divisions to emphasis pattern`() {
    assertThat(listOf(1, 0).toEmphasisPattern()).isEqualTo(listOf(1))
    assertThat(listOf(3, 2, 2, 0).toEmphasisPattern()).isEqualTo(listOf(1, 0, 0, 1, 0, 1, 0))
  }

  private fun getMetronomeController() = MetronomeController(
    rhythmRepository = repo,
    clock = clock,
    player = player,
    ioScope = testScope
  )
}
