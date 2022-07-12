package com.jedparsons.metronome.player

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.jedparsons.metronome.repo.RhythmData.Updated
import com.jedparsons.metronome.repo.RhythmRepository
import com.jedparsons.metronome.util.MetronomeClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

/**
 * A controller to drive the native metronome.
 *
 * Observes the [RhythmRepository] for updates to the meter and play/pause state.
 *
 * Subscribes to application lifecycle callbacks, which provide the hooks for starting and stopping
 * the audio system.
 */
class MetronomeController(
  private val rhythmRepository: RhythmRepository,
  private val clock: MetronomeClock,
  private val player: MetronomePlayer,
  ioScope: CoroutineScope
) : ActivityLifecycleCallbacks {

  private var timer: Timer? = null
  private var lastBeat: Long = 0
  private var currentEmphasis = 0
  private var emphasis: List<Int> = listOf(1)

  private var bpm: Int = 120
  private var subdivisions: List<Int> = listOf(1)
    set(value) {
      field = value
      emphasis = field.toEmphasisPattern()
    }

  init {
    ioScope.launch {
      rhythmRepository.playing.collect { play ->
        if (play) play() else stop()
      }
    }

    ioScope.launch {
      rhythmRepository.rhythm.collect { rhythm ->
        (rhythm as? Updated)?.let { data ->
          bpm = data.rhythmModel.bpm
          subdivisions = data.rhythmModel.divisions
        }
      }
    }
  }

  fun play() {
    if (timer != null) return

    stop()
    val t = Timer()
    timer = t
    // Loop forever. On each iteration, see if it's time to play a beat, and with what emphasis.
    t.schedule(object : TimerTask() {
      override fun run() {
        val now = clock.currentTimeMillis()
        if (now >= (60000 / bpm) + lastBeat) {
          player.playClick()
          lastBeat = now
          if (currentEmphasis >= emphasis.size) {
            currentEmphasis = 0
          }
          player.setGain(if (emphasis[currentEmphasis] == 1) MAX_GAIN else LOW_GAIN)
          currentEmphasis += 1
        }
      }
    }, 0, 30)
  }

  fun stop() {
    timer?.cancel()
    timer = null
  }

  override fun onActivityStarted(activity: Activity) {
    player.setUp(activity.assets)
  }

  override fun onActivityStopped(activity: Activity) {
    player.tearDown()
  }

  override fun onActivityResumed(activity: Activity) = Unit

  override fun onActivityPaused(activity: Activity) = Unit

  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

  override fun onActivityDestroyed(activity: Activity) = Unit

  companion object {
    const val MAX_GAIN = 1.8f
    const val LOW_GAIN = 0.4f
  }
}

/**
 * Map the subdivisions from the view model to a list of 1s and 0s, one per each beat.
 * E.g. (4, 2, 3) -> (1, 0, 0, 0, 1, 0, 1, 0, 0)
 *
 * We'll just loop through this processed list when setting the gain for each beat.
 */

fun List<Int>.toEmphasisPattern() = this.filter { it > 0 }
  .map { v ->
    mutableListOf(1).apply {
      repeat(v - 1) { this += 0 }
    }
  }.flatten()
