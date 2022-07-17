package com.jedparsons.metronome

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.jedparsons.metronome.player.RealMetronomePlayer
import com.jedparsons.metronome.repo.RhythmRepository
import com.jedparsons.metronome.storage.RhythmStore
import com.jedparsons.metronome.storage.SharedPrefsRhythmStore
import com.jedparsons.metronome.util.MetronomeClock
import com.jedparsons.metronome.util.RealMetronomeClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Dependency injection.
 */
interface AppContainer {
  val mainScope: CoroutineScope
  val ioScope: CoroutineScope
  val rhythmRepo: RhythmRepository
  val clock: MetronomeClock
  val player: RealMetronomePlayer
}

const val SHARED_PREFS = "metronome-prefs"

class RealAppContainer(
  private val context: Context
) : AppContainer {

  private val sharedPrefs: SharedPreferences by lazy {
    context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
  }

  private val rhythmStore: RhythmStore by lazy {
    SharedPrefsRhythmStore(sharedPrefs)
  }

  override val mainScope: CoroutineScope by lazy {
    CoroutineScope(Dispatchers.Main)
  }

  override val ioScope: CoroutineScope by lazy {
    CoroutineScope(Dispatchers.IO)
  }

  override val rhythmRepo: RhythmRepository by lazy {
    RhythmRepository(rhythmStore)
  }

  override val clock: MetronomeClock by lazy {
    RealMetronomeClock()
  }

  override val player: RealMetronomePlayer by lazy {
    RealMetronomePlayer()
  }
}
